package controllers;

import controllers.security.AuthAdmin;
import controllers.security.Secured;
import play.data.Form;
import play.data.FormFactory;
import play.db.ebean.Transactional;
import play.mvc.*;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import play.api.Environment;

import views.html.admin.*;
import models.*;
import models.users.User;

import play.mvc.Http.*;
import play.mvc.Http.MultipartFormData.FilePart;
import java.io.File;

// File upload and image editing dependencies
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;


// Require Login
@Security.Authenticated(Secured.class)
// Authorise user (check if admin)
@With(AuthAdmin.class)
public class AdminController extends Controller {

    // Declare a private FormFactory instance
    private FormFactory formFactory;

    private Environment env;

    
    //  Inject an instance of FormFactory it into the controller via its constructor
    @Inject
    public AdminController(FormFactory f,  Environment e) {
        this.formFactory = f;
        this.env = e;
    }

    // Method returns the logged in user (or null)
    private User getUserFromSession() {
        return User.getUserById(session().get("email"));
    }


    public Result products(Long cat) {

        // Get list of all categories in ascending order
        List<Category> categoriesList = Category.findAll();
        List<Product> productsList = new ArrayList<Product>();

        if (cat == 0) {
            // Get list of all categories in ascending order
            productsList = Product.findAll();
        }
        else {
            // Get products for selected category
            // Find category first then extract products for that cat.
            productsList = Category.find.ref(cat).getProducts();
        }

        return ok(products.render(productsList, categoriesList, getUserFromSession(), env));
    }

    // Render and return  the add new product page
    // The page will load and display an empty add product form

    public Result addProduct() {

        // Create a form by wrapping the Product class
        // in a FormFactory form instance
        Form<Product> addProductForm = formFactory.form(Product.class);

        // Render the Add Product View, passing the form object
        return ok(addProduct.render(addProductForm, getUserFromSession()));
    }

    @Transactional
    public Result addProductSubmit() {

        String saveImageMsg;

        // Create a product form object (to hold submitted data)
        // 'Bind' the object to the submitted form (this copies the filled form)
        Form<Product> newProductForm = formFactory.form(Product.class).bindFromRequest();

        // Check for errors (based on Product class annotations)
        if(newProductForm.hasErrors()) {
            // Display the form again
            return badRequest(addProduct.render(newProductForm, getUserFromSession()));
        }

        // Extract the product from the form object
        Product p = newProductForm.get();

        if (p.getId() == null) {
            // Save to the database via Ebean (remember Product extends Model)
            p.save();
        }
        // Product already exists so update
        else if (p.getId() != null) {
            p.update();
        }
        

        //save image
        //get the image data
        MultipartFormData data = request().body().asMultipartFormData();
        FilePart image = data.getFile("upload");

        //save the image file
        saveImageMsg = saveFile(p.getId(), image);
        



        // Set a success message in temporary flash
        // for display in return view
        flash("success", "Product " + p.getName() + " has been created/ updated" + saveImageMsg);

        // Redirect to the admin home
        return redirect(routes.AdminController.products(0));
    }

    // Update a product by ID
    // called when edit button is pressed
    @Transactional
    public Result updateProduct(Long id) {

        Product p;
        Form<Product> productForm;

        try {
            // Find the product by id
            p = Product.find.byId(id);

            // Create a form based on the Product class and fill using p
            productForm = formFactory.form(Product.class).fill(p);

            } catch (Exception ex) {
                // Display an error message or page
                return badRequest("error");
        }
        // Render the updateProduct view - pass form as parameter
        return ok(addProduct.render(productForm, getUserFromSession()));
    }

    // Delete Product by id
    @Transactional
    public Result deleteProduct(Long id) {

        // find product by id and call delete method
        Product.find.ref(id).delete();
        // Add message to flash session
        flash("success", "Product has been deleted");

        // Redirect to products page
        return redirect(routes.AdminController.products(0));
    }

   public String saveFile(Long id, FilePart<File> image) {
       if (image != null) {

           String mimeType = image.getContentType();

           //check if this ia an image
           if (mimeType.startsWith("image/")) {

               File file = image.getFile();

               ConvertCmd cmd = new ConvertCmd();

               IMOperation op = new IMOperation();

               op.addImage(file.getAbsolutePath());

               op.resize(300, 200);

               op.addImage("public/images/productImages/" + id + ".jpg");

               IMOperation thumb = new IMOperation();

               thumb.addImage(file.getAbsolutePath());
               thumb.thumbnail(60);

               thumb.addImage("public/images/productImages/thumbnails/" + id + ".jpg");

               try {
                   cmd.run(op);
                   cmd.run(thumb);
               } catch (Exception e) {
                  // e.printStacktrace();
               }
               return " and image saved";
           }
       }
       return "image file missing";
   }





}
