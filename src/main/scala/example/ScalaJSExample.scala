package example

import org.scalajs.dom
import scalatags.all._
import scalatags.Tags2.section
import scalatags.ExtendedString
import rx._


case class Task(txt: Var[String], done: Var[Boolean])
object ScalaJSExample {
  import Framework._

  val tasks = Var(
    Seq(
      Task(Var("Create a TodoMVC template D"), Var(true)),
      Task(Var("Create a TodoMVC template B"), Var(false)),
      Task(Var("Create a TodoMVC template C"), Var(false))
    )
  )

  val filters: Map[String, Task => Boolean] = Map(
    ("All", t => true),
    ("Active", !_.done()),
    ("Completed", _.done())
  )

  val filter = Var("All")

  val inputBox = new DomRef[dom.HTMLInputElement](input(
    id:="new-todo",
    placeholder:="What needs to be done?",
    autofocus:=true
  ))

  val editing = Var[Option[Task]](None)

  def main(): Unit = {
    dom.document.body.innerHTML = Seq(
      section(id:="todoapp")(
        header(id:="header")(
          h1("todos"),
          form(
            inputBox,
            onsubmit <~ {
              tasks() = Task(Var(inputBox.value), Var(false)) +: tasks()
              inputBox.value = ""
            }
          )
        ),
        section(id:="main")(
          input(
            id:="toggle-all",
            `type`:="checkbox",
            cursor:="pointer",
            onclick <~ {
              val target = tasks().exists(_.done() == false)
              tasks().map(_.done() = target)
            }
          ),
          label(`for`:="toggle-all", "Mark all as complete"),
          Rx{
            dom.console.log("A")
            ul(id:="todo-list")(
              for(task <- tasks() if filters(filter())(task)) yield Rx{
                dom.console.log("B", task.txt())
                val inputRef = new DomRef[dom.HTMLInputElement](
                  input(`class`:="edit", value:=task.txt())
                )
                li(if(task.done()) `class`:="completed" else (), if(editing() == Some(task)) `class`:="editing" else ())(
                  div(`class`:="view")(
                    "ondblclick".attr <~ {editing() = Some(task)},
                    input(
                      `class`:="toggle",
                      `type`:="checkbox",
                      cursor:="pointer",
                      onchange <~ {task.done() = !task.done()},
                      if(task.done()) checked:=true else ()
                    ),
                    label(Rx(task.txt())),
                    button(
                      `class`:="destroy",
                      cursor:="pointer",
                      onclick <~ (tasks() = tasks().filter(_ != task))
                    )
                  ),
                  form(
                    onsubmit <~ {
                      task.txt() = inputRef.value
                      editing() = None
                    },
                    inputRef
                  )
                )
              }
            )
          },
          footer(id:="footer")(
            span(id:="todo-count")(strong(Rx(tasks().count(!_.done()).toString)), " item left"),
            Rx{
              ul(id:="filters")(
                for ((name, pred) <- filters.toSeq) yield {
                  li(a(
                    if(name == filter()) `class`:="selected" else (),
                    name,
                    href:="#",
                    onclick <~ (filter() = name)
                  ))
                }
              )
            },
            button(
              id:="clear-completed",
              onclick <~ {tasks() = tasks().filter(!_.done())},
              "Clear completed (", Rx(tasks().count(_.done()).toString), ")"
            )
          )
        ),
        footer(id:="info")(
          p("Double-click to edit a todo"),
          p("Template by ", a(href:="http://github.com/lihaoyi")("Li Haoyi")),
          p("Created by ", a(href:="http://github.com/lihaoyi")("Li Haoyi"))
        )
      )
    ).mkString
  }
}