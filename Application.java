import java.util.ArrayList;
import java.util.Map;

public class Application {

    ArrayList<User> allUsers = new ArrayList<>();
    ArrayList<Sheet> allSheets = new ArrayList<>();
    // TO DO: unclear name
    Map<Client, User> clientsToUsers;

    public Application(ArrayList<User> allUsers, ArrayList<Sheet> allSheets) {
        this.allUsers = allUsers;
        this.allSheets = allSheets;
    }

    private void createNewClient() {

        Client newClient = new Client(this);
        clientsToUsers.put(newClient, null);
    }

    public void tryNewUserLogin(Client client, String username, String password) {
        if(existingUser(username)) {
            client.rejectNewUser();
        }
        else {
            User newUser = new User(username, password);

            clientsToUsers.put(client, newUser);
            client.acceptNewUser(newUser);
        }
    }

    public void tryExistingUserLogin(Client client, String username, String password) {

        if(existingUser(username)) {
            User existingUser = getUser(username);

            if(existingUser.isPassword(password)) {
                clientsToUsers.put(client, existingUser);
                client.acceptExistingUser(existingUser);
            }
            else {
                client.rejectExistingUser();
            }
        }
        else {
            clientsToUsers.put(client, new User(username, password));
        }
    }

    //Checks if the given username cooresponds to an existing existingUser
    private boolean existingUser(String username) {
        for (User existingUser : allUsers) {
            if(existingUser.isUsername(username)) {
                return true;
            }
        }
        return false;
    }

    private User getUser(String username) {
        for (User existingUser : allUsers) {
            if(existingUser.isUsername(username)) {
                return existingUser;
            }
        }
        
        throw new IllegalStateException("Given username does not coorespond to existing existingUser");
    }

    public Sheet newSheet(Client client) {
        Sheet sheet = new Sheet("");
        User existingUser = clientsToUsers.get(client);

        sheet.giveUserAccess(existingUser);
        allSheets.add(sheet);

        return sheet;
    }

    public ArrayList<Sheet> sheetsUserCanAccess(Client client) {
        User existingUser = clientsToUsers.get(client);
        return existingUser.accessibleSheets(allSheets);
    }

    public ArrayList<String> sheetNamesToDisplay(User existingUser) {
        ArrayList<String> result = new ArrayList<>();

        for(Sheet sheet : existingUser.accessibleSheets(allSheets)) {
            result.add(sheet.name());
        }

        return result;
    }

}
