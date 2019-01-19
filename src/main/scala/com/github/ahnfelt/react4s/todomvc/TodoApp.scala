package com.github.ahnfelt.react4s.todomvc
import com.github.ahnfelt.react4s._
import scala.scalajs.js.annotation._
import org.scalajs.dom.ext.LocalStorage
import org.scalajs.dom.window

@JSExportTopLevel("TodoApp")
object TodoApp {
    @JSExport
    def run() : Unit = ReactBridge.renderToDomById(Component(TodoListComponent), "main")
    val localStorageKey = "todos-react4s"
}

case class TodoItem(id : Int, title : String, completed : Boolean)

case class TodoListComponent() extends Component[NoEmit] {

    val text = State("")
    val items = State(LocalStorage(TodoApp.localStorageKey).toList.flatMap(upickle.default.read[List[TodoItem]]))
    val filterCompleted = AddEventListener(this, window, "hashchange", _ => window.location.hash) {
        case ("#/active", _) => Some(false)
        case ("#/completed", _) => Some(true)
        case (_, _) => None
    }

    def modifyItems(get : Get, body : List[TodoItem] => List[TodoItem]) : Unit = {
        items.modify(body)
        LocalStorage(TodoApp.localStorageKey) = upickle.default.write(get(items))
    }

    def nextId(get : Get) = (0 :: get(items).map(_.id)).max + 1

    override def render(get : Get) = {
        Fragment(
            E.section(A.className("todoapp"),
                E.header(A.className("header"),
                    E.h1(Text("todos")),
                    E.form(
                        E.input(A.className("new-todo"),
                            A.placeholder("What needs to be done?"),
                            A.autoFocus(),
                            A.value(get(text)),
                            A.onChangeText(text.set)
                        ),
                        A.onSubmit { e =>
                            e.preventDefault()
                            val item = TodoItem(id = nextId(get), title = get(text).trim, completed = false)
                            if(item.title.nonEmpty) {
                                modifyItems(get, item :: _)
                                text.set("")
                            }
                        }
                    )
                ),
                E.section(A.className("main"),
                    E.input(A.id("toggle-all"), A.className("toggle-all"), A.`type`("checkbox"), A.onLeftClick { _ =>
                        val check = get(items).exists(!_.completed)
                        modifyItems(get, _.map(_.copy(completed = check)))
                    }).when(get(items).nonEmpty),
                    E.label(A.`for`("toggle-all"), Text("Mark all as complete")).when(get(items).nonEmpty),
                    E.ul(A.className("todo-list"),
                        Tags(
                            for {
                                (item, i) <- get(items).zipWithIndex
                                if get(filterCompleted).forall(_ == item.completed)
                            } yield {
                                Component(TodoItemComponent, item).withHandler {
                                    case Some(editedItem) => modifyItems(get, _.updated(i, editedItem))
                                    case None => modifyItems(get, list => list.take(i) ++ list.drop(i + 1))
                                }.withKey("item-" + item.id)
                            }
                        )
                    ).when(get(items).nonEmpty)
                ),
                E.footer(A.className("footer"),
                    {
                        val incomplete = get(items).count(!_.completed)
                        E.span(A.className("todo-count"),
                            E.strong(Text(incomplete + " ")),
                            Text(if(incomplete == 1) "item left" else "items left")
                        )
                    },
                    E.ul(A.className("filters"),
                        E.li(E.a(A.className("selected").when(get(filterCompleted).isEmpty),
                            A.href("#/"),
                            Text("All")
                        )),
                        E.li(E.a(A.className("selected").when(get(filterCompleted).contains(false)),
                            A.href("#/active"),
                            Text("Active")
                        )),
                        E.li(E.a(A.className("selected").when(get(filterCompleted).contains(true)),
                            A.href("#/completed"),
                            Text("Completed")
                        ))
                    ),
                    E.button(A.className("clear-completed"),
                        Text("Clear completed"),
                        A.onLeftClick { _ => modifyItems(get, _.filterNot(_.completed)) }
                    )
                ).when(get(items).nonEmpty)
            ),
            E.footer(A.className("info"),
                E.p(Text("Double-click to edit a todo")),
                E.p(E.a(A.href("http://www.react4s.org"), Text("Written in Scala with React4s"))),
                E.p(
                    Text("Implements all of "),
                    E.a(A.href("http://todomvc.com/"), Text("TodoMVC")),
                    Text(" in "), E.a(A.href("https://github.com/ahnfelt/react4s-todomvc"), Text("139 lines"))
                )
            )
        )
    }
}

case class TodoItemComponent(item : P[TodoItem]) extends Component[Option[TodoItem]] {
    val editing = State(false)
    override def render(get : Get) = {
        E.li(A.className("completed").when(get(item).completed), A.className("editing").when(get(editing)),
            E.div(A.className("view"),
                E.input(A.className("toggle"),
                    A.`type`("checkbox"),
                    A.checked().when(get(item).completed),
                    A.onLeftClick(_ => emit(Some(get(item).copy(completed = !get(item).completed))))
                ),
                E.label(Text(get(item).title)),
                E.button(A.className("destroy"), A.onLeftClick(_ => emit(None))),
                A.on("DoubleClick", _ => editing.set(true))
            ),
            E.input(A.className("edit"),
                A.value(get(item).title),
                A.onChangeText(text => emit(Some(get(item).copy(title = text)))),
                A.autoFocus(),
                A.onBlur(_ => editing.set(false))
            ).when(get(editing))
        )
    }
}
