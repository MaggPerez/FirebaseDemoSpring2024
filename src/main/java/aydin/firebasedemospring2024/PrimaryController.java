package aydin.firebasedemospring2024;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PrimaryController {
    @FXML
    private TextField ageTextField;

    @FXML
    private TextField nameTextField;

    @FXML
    private TextArea outputTextArea;

    @FXML
    private Button readButton;

    @FXML
    private Button registerButton;

    @FXML
    private Button switchSecondaryViewButton;

    @FXML
    private Button writeButton;

    private boolean key;
    private ObservableList<Person> listOfUsers = FXCollections.observableArrayList();
    private Person person;

    public ObservableList<Person> getListOfUsers() {
        return listOfUsers;
    }




    //******************************************************
    // New Additions
    //******************************************************
    @FXML
    private TextField createEmailTextField;

    @FXML
    private TextField createPassTextField;

    @FXML
    private TextField createPhoneNumberTextField;


    String getNewEmail;
    String getNewPassword;



    void initialize() {

        AccessDataView accessDataViewModel = new AccessDataView();
        nameTextField.textProperty().bindBidirectional(accessDataViewModel.personNameProperty());
        writeButton.disableProperty().bind(accessDataViewModel.isWritePossibleProperty().not());
    }


    @FXML
    void readButtonClicked(ActionEvent event) {
        readFirebase();
    }

    @FXML
    void registerButtonClicked(ActionEvent event) throws IOException {
        registerUser();
        switchToPrimary();
    }


    @FXML
    void writeButtonClicked(ActionEvent event) {
        addData();
    }

    @FXML
    private void switchToSecondary() throws IOException {
        DemoApp.setRoot("welcome-screen");
    }
    public boolean readFirebase()
    {
        key = false;

        //asynchronously retrieve all documents
        ApiFuture<QuerySnapshot> future =  DemoApp.fstore.collection("Persons").get();
        // future.get() blocks on response
        List<QueryDocumentSnapshot> documents;
        try
        {
            documents = future.get().getDocuments();
            if(documents.size()>0)
            {
                System.out.println("Getting (reading) data from firabase database....");
                listOfUsers.clear();
                for (QueryDocumentSnapshot document : documents)
                {
                    outputTextArea.setText(outputTextArea.getText()+ document.getData().get("Name")+ " , Age: "+
                            document.getData().get("Age")+ " PhoneNumber: " +
                            document.getData().get("PhoneNumber")+ " \n ");
                    System.out.println(document.getId() + " => " + document.getData().get("Name"));
                    person  = new Person(String.valueOf(document.getData().get("Name")),
                            Integer.parseInt(document.getData().get("Age").toString()),
                            String.valueOf(document.getData().get("PhoneNumber")));
                    listOfUsers.add(person);
                }
            }
            else
            {
                System.out.println("No data");
            }
            key=true;

        }
        catch (InterruptedException | ExecutionException ex)
        {
            ex.printStackTrace();
        }
        return key;
    }



    //My Version
    public boolean registerUser() {


        //Getting New email and password from user
        getNewEmail = createEmailTextField.getText();
        getNewPassword = createPassTextField.getText();


        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(getNewEmail)
                .setEmailVerified(false)
                .setPassword(getNewPassword)
//                .setPhoneNumber("+11234567891")
//                .setDisplayName("John Doe")
                .setDisabled(false);

        DocumentReference docRef = DemoApp.fstore.collection("Passwords").document(UUID.randomUUID().toString());

        Map<String, Object> data = new HashMap<>();
        data.put("Password", createPassTextField.getText());

        //asynchronously write data
        ApiFuture<WriteResult> result = docRef.set(data);



        UserRecord userRecord;
        try {
            userRecord = DemoApp.fauth.createUser(request);
            System.out.println("Successfully created new user with Firebase Uid: " + userRecord.getUid()
                    + " check Firebase > Authentication > Users tab");
            return true;

        } catch (FirebaseAuthException ex) {
            // Logger.getLogger(FirestoreContext.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error creating a new user in the firebase");
            return false;
        }



    }


    public void addData() {

        DocumentReference docRef = DemoApp.fstore.collection("Persons").document(UUID.randomUUID().toString());

        Map<String, Object> data = new HashMap<>();
        data.put("Name", nameTextField.getText());
        data.put("Age", Integer.parseInt(ageTextField.getText()));
        //new
        data.put("PhoneNumber", createPhoneNumberTextField.getText());

        //asynchronously write data
        ApiFuture<WriteResult> result = docRef.set(data);
    }



    //******************************************************
    // New Methods
    //******************************************************

    @FXML
    private TextField userTextField;

    @FXML
    private TextField passTextField;
    public void switchToPrimaryAfterLogin() throws IOException{
        String email = userTextField.getText();
        String usersPassword = passTextField.getText();
        String getFireBaseEmail;


        //Getting String password from firebase
        String fireBasePassword;


        UserRecord userRecordEmail;



        try {
            //Getting email from database and putting it into variable
            userRecordEmail = DemoApp.fauth.getUserByEmail(email);

            getFireBaseEmail = userRecordEmail.getEmail();


            //******************************************************
            // Authenticating Username and Password
            //******************************************************
            ApiFuture<QuerySnapshot> future =  DemoApp.fstore.collection("Passwords").get();

            List<QueryDocumentSnapshot> documents;
            try
            {
                documents = future.get().getDocuments();
                if(documents.size()>0)
                {
                    for (QueryDocumentSnapshot document : documents)
                    {
                        fireBasePassword = String.valueOf(document.getData().get("Password"));

                        //If user's typed email matches in the database and user's type password matches
                        // in database, they have successfully logged in.
                        if(getFireBaseEmail.equals(email) && usersPassword.equals(fireBasePassword)){
                            System.out.println("Logged in Successfully");
                            DemoApp.setRoot("primary");
                        }

                    }
                }
                else
                {
                    System.out.println("No data");
                }


            }
            catch (InterruptedException | ExecutionException ex)
            {
                ex.printStackTrace();
            }

            System.out.println(userRecordEmail.getEmail());
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }


    }

    private void switchToPrimary() throws IOException {
        DemoApp.setRoot("primary");
    }



}