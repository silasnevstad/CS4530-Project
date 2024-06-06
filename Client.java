import java.util.ArrayList;

public class Client {

    Application controller;

    public Client(Application application) {
        this.controller = application;
    }


    private void createWelcomePage() {
        // create welcome page window

        // display welcome message and instructions to login or create new user

        // create one button to for a new user to login and another for an existing user to login

        // link "new/existing user login buttons" to call respective methods when either button is pressed
    }

    private void loginNewUser() {
        // create new user login page gui

        // display new user welcome message and instructions to create new account/user

        // add textboxes for username and password

        // add "enter" button

        // link "enter" button to call application's tryNewUserLogin method with the username
        // and password in the textboxes
    }

    private void loginExistingUser() {
        // create existing user login page gui

        // display existing user welcome message and instructions to enter existing username and password

        // add textboxes for username and password 
        
        // link textbox entries to respective strings
        String username = null; // TO DO: fill in with actual link to textboxes
        String password = null;

        // call controller's updateCurrentUser with the user information as input
        controller.tryExistingUserLogin(this, username, password);

        // call controller's sheetsUserCanAccess method to determine which sheets the user has access to

        // give the list of sheets that the user can access to the method that displays sheets the user has access to

    }

    public void acceptNewUser(User user) {
        createSheetSelectPage(user);
    }

    public void rejectNewUser() {
        // create rejected new user page window

        // display message that username already exists for existing user

        // create one button to go back to welcome login page

        // link button to call createWelcomePage
    }

    public void acceptExistingUser(User user) {
        createSheetSelectPage(user);
    }

    public void rejectExistingUser() {
        // create rejected existing user page window

        // display message that username/password was rejected for logging in existing user

        // create one button to go back to welcome login page

        // link button to call createWelcomePage
    }

    private void createSheetSelectPage(User user) {
        // create existing sheet select page gui

        // display sheet select message and instructions to either select a sheet to edit or create a new sheet

        // add "new sheet" button at the top

        // link "new sheet" button to createNewSheet method
            // TO DO: should this ask for a name for the sheet at this point or should the user be able to create 
            //        untitled sheet and name it later?

        // call controller's sheetsToDisplay method to get which sheets to display
        ArrayList<String> sheetNamesToDisplay = controller.sheetNamesToDisplay(user);

        // for each sheet given in the list, add a button with the name of that sheet on it
        
        // link buttons to openSheet method with respective sheet as the input
    }


    // TO DO: Should we add similar method that takes in a String for the name of the sheet to be created and
    //        does the same thing?
    private void createNewSheet() {
        // call newSheet method in application with this as the input
        Sheet newSheet = controller.newSheet(this);

        // create new spreadsheet using to-be-implemented spreadsheet UI giving it newSheet as input
    }

    private void openSheet(Sheet sheet) {
        // open existing spreadsheet using to-be-implemented spreadsheet UI giving it sheet as input
    }
}
