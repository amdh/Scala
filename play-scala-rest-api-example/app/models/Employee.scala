package models
import play.api.libs.json._

object Employee {

  implicit val employeeWrites = Json.writes[Employee]
  implicit  val employeeFormat = Json.format[Employee]

}


case class Employee(
                     id : Long,
                     name: String,
                     company: String)


