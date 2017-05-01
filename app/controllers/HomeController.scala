package controllers

import javax.inject.Inject

import models._
import play.api.data.Forms._
import play.api.data._
import play.api.i18n._
import play.api.mvc._
import views._

/**
  * Manage a database of computers
  */
class HomeController @Inject()(computerService: ComputerService,
                               companyService: CompanyService,
                               val messagesApi: MessagesApi)
  extends Controller with I18nSupport {

  /**
    * This result directly redirect to the application home.
    */
  val Home = Redirect(routes.HomeController.list(0, 2, ""))

  /**
    * Describe the computer form (used in both edit and create screens).
    */
  val computerForm = Form(
    mapping(
      "id" -> ignored(None: Option[Long]),
      "name" -> nonEmptyText,
      "introduced" -> optional(date("yyyy-MM-dd")),
      "discontinued" -> optional(date("yyyy-MM-dd")),
      "company" -> optional(longNumber),
      "image" -> optional(longNumber)
    )(Computer.apply)(Computer.unapply)
  )

  // -- Actions

  /**
    * Handle default path requests, redirect to computers list
    */
  def index = Action {
    Home
  }

  /**
    * Display the paginated list of computers.
    *
    * @param page    Current page number (starts from 0)
    * @param orderBy Column to be sorted
    * @param filter  Filter applied on computer names
    */
  def list(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    val filterQuery = "%" + filter + "%"
    Ok(html.list(
      computerService.list(page = page, orderBy = orderBy, filter = filterQuery),
      orderBy, filter
    ))
  }

  /**
    * Display the 'edit form' of a existing Computer.
    *
    * @param id Id of the computer to edit
    */
  def edit(id: Long) = Action {
    computerService.findFullById(id).map { item =>
      val imgLink = item.image.map(_.link).getOrElse("https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcRZqiIAtBVyfHgk5gnR-g91pdsHFBtieR72rLOsJJOgw2VONt-9-lbNXrr_ow")
      Ok(html.editForm(id, computerForm.fill(item.computer), imgLink, companyService.options))
    }.getOrElse(NotFound)
  }

  /**
    * Handle the 'edit form' submission
    *
    * @param id Id of the computer to edit
    */
  def update(id: Long) = Action { implicit request =>
    computerForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.editForm(id, formWithErrors, "", companyService.options)),
      computer => {
        computerService.update(id, computer)
        Home.flashing("success" -> "Computer %s has been updated".format(computer.name))
      }
    )
  }

  /**
    * Display the 'new computer form'.
    */
  def create = Action {
    Ok(html.createForm(computerForm, companyService.options))
  }

  /**
    * Handle the 'new computer form' submission.
    */
  def save = Action { implicit request =>
    computerForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.createForm(formWithErrors, companyService.options)),
      computer => {
        computerService.insert(computer)
        Home.flashing("success" -> "Computer %s has been created".format(computer.name))
      }
    )
  }

  /**
    * Handle computer deletion.
    */
  def delete(id: Long) = Action {
    computerService.delete(id)
    Home.flashing("success" -> "Computer has been deleted")
  }
}
            
