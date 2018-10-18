package controllers

import models.Employee
import javax.inject.Inject
import anorm._
import anorm.SqlParser.get
import anorm.{Row, SQL, SimpleSql, ~}
import play.api.mvc._
import play.api.db._
import play.api.libs.json._

class EmployeeController @Inject()(db: Database, val controllerComponents: ControllerComponents) extends BaseController{

//write employee object in json format

  val rowParser: anorm.RowParser[Employee] = {
    get[Long] ("id") ~
      get[String]("name") ~
      get[String]("company") map {
      case id~name~comp =>Employee(id, name, comp)
    }
  }

//dummy list of employees
  var employees = List(
    Employee(1, "Mark","abc"),
    Employee(2,"Roy","xyz")

  )

  //return single employee depending upon id
  def getEmployee(id : Long) = Action {

    //val emp = Json.toJson(Employee(3,"Zoya","QQQ"))
    //Ok(emp)
    val emp : Option[Employee] = db.withConnection { implicit c =>
       SQL("Select * from employee where id = ({id});").on('id -> id).as(rowParser.singleOpt)

    }
      println(emp)
      val employeesJSON = Json.toJson(emp)

    Ok(employeesJSON)
  }

  //return list of  employees
  def getEmployees = Action {


    // val employeesJSON = Json.toJson(employees)
    //println(" getEmployees executed"+employeesJSON)


    //OR

    var outString = "Employees are:\n "
    val conn = db.getConnection()

    try {
      val stmt = conn.createStatement
      val rs = stmt.executeQuery("SELECT name,company from employee ")

      println(rs)
      while (rs.next()) {
        outString += rs.getString("name")
        outString += ","
        outString += rs.getString("company")
        outString += "--"
      }
    } finally {
      conn.close()
    }

    println(" getEmployee executed"+outString)
    Ok(outString)


    //OR

//   db.withConnection { implicit c =>
//      val employees = SQL("Select * From employee;")
//     println("employees:" + employees )
//      // Transform the resulting Stream[Row] to a List[(id,String,String)]
//      val empList =  employees().map(row => Employee(row[Long]("id"), row[String]("name"), row[String]("company"))).toList
//     println("employees list:" + empList )
//      empList
//
//    }
//    Ok("")
  }

  //post call get values from url parameters
  def postEmployee( name: String, company: String) = Action{
     val emp  = Employee(4,name, company)
    Ok{ Json.toJson(emp)  }
  }

  //put call get values from url parameters
  def putEmployee = Action{request =>

    val json = request.body.asJson.get
    val emp = json.as[Employee]
    println(emp)

    db.withConnection { implicit c =>
      SQL("update employee set name={name}, company={company} where id={id}")
        .on('name -> emp.name, 'company -> emp.company, 'id -> emp.id)
        .executeUpdate()
    }

    Ok{"successfully updated!"}
  }


  //delete employee

  def deleteEmployee( id: Long) = Action {

    db.withTransaction { implicit c =>
      val res1: Int = SQL"delete from employee where id = $id".executeUpdate()
    }
    Ok{"successfully deleted!"}
  }



  //post call get values from post body
   def saveEmp = Action { request =>
    val json = request.body.asJson.get
    val emp = json.as[Employee]
    println(emp)

    val id: Option[Long] = db.withConnection { implicit c =>
      SQL("insert into employee (id, name, company) values ({id}, {name}, {company})")
        .on(
          "id" -> emp.id,
          "name" -> emp.name.toUpperCase,
          "company" -> emp.company)
        .executeInsert()
    }
     println("saved employee!")
    Ok{"successfully saved!" + id}
  }

}
