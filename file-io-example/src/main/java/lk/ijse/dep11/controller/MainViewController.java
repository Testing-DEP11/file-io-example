package lk.ijse.dep11.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import lk.ijse.dep11.tm.Employee;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MainViewController {
    public AnchorPane root;
    public Button btnNew;
    public TextField txtId;
    public TextField txtName;
    public TextField txtContact;
    public TableView<Employee> tblEmployee;
    public TextField txtSearch;
    public Button btnSave;
    public Button btnDelete;

    public ArrayList<Employee> employeesList = new ArrayList<>();
    public List<Employee> filteredList;

    public void initialize() {

        for (Control control : new Control[]{txtId, txtName, txtContact, btnSave, btnDelete}) {
            control.setDisable(true);
        }

        tblEmployee.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblEmployee.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblEmployee.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("contact"));

        tblEmployee.getSelectionModel().selectedItemProperty().addListener((o, old, current) -> {
            if(current == null) {
                btnDelete.setDisable(true);
            } else {
                txtId.setText(current.getId());
                txtName.setText(current.getName());
                txtContact.setText(current.getContact());
                btnDelete.setDisable(false);
            }
        });

        Platform.runLater(() -> {
            root.getScene().getWindow().setOnCloseRequest(event -> {
                saveEmployeeList();
            });
        });
        employeesList = readEmployeeList();
        ObservableList<Employee> observableEmployeeList = FXCollections.observableList(employeesList);
        tblEmployee.setItems(observableEmployeeList);


        /*FilteredList<Employee> filteredList = new FilteredList<>(observableEmployeeList);
        tblEmployee.setItems(filteredList);

        txtSearch.textProperty().addListener((o,old, current) -> {
            filteredList.setPredicate(yourData -> {
                if(current == null || current.isEmpty()) return true;
                String pattern = ".*" + current.toLowerCase() + ".*";
                if(yourData.getId().toLowerCase().matches(pattern)) return true;
                else if(yourData.getName().toLowerCase().matches(pattern)) return true;
                else if(yourData.getContact().toLowerCase().matches(pattern)) return true;
                else return false;
            });
        });*/

        filteredList = new ArrayList<>(observableEmployeeList);
        tblEmployee.setItems((ObservableList<Employee>) filteredList);

        txtSearch.textProperty().addListener((o, old, current) -> {
            if (current == null || current.isEmpty()) {
                filteredList.addAll(observableEmployeeList);
            } else {
                String pattern = ".*" + current.toLowerCase() + ".*";
                List<Employee> newList = observableEmployeeList.filtered(yourData ->
                        yourData.getId().toLowerCase().matches(pattern) ||
                                yourData.getName().toLowerCase().matches(pattern) ||
                                yourData.getContact().toLowerCase().matches(pattern)
                );
                filteredList.addAll(newList);
            }
        });


    }

    private ArrayList<Employee> readEmployeeList() {
        File databaseFile = new File("employee.db");
        if(!databaseFile.exists()) return new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(databaseFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis);
            try {
                return (ArrayList<Employee>) ois.readObject();
            } finally {
                ois.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to read employee database").show();
            return new ArrayList<>();
        }
    }

    private void saveEmployeeList() {
        File databaseFile = new File("employee.db");

        try {
            databaseFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(databaseFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            try {
                oos.writeObject(employeesList);
            } finally {
                oos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to save the file.").show();
        }

    }

    public void btnNewOnAction(ActionEvent actionEvent) {
        txtId.setText(getNewEmployeeId());
        for (Control control : new Control[]{txtName, txtContact, btnSave}) {
            if(control instanceof TextField) ((TextField) control).clear();
            control.setDisable(false);
        }
        txtName.requestFocus();
        tblEmployee.getSelectionModel().clearSelection();
    }

    private String getNewEmployeeId() {
        if(getEmployeeList().isEmpty()) return "E-001";
        else {
            int newEmployeeId = Integer.parseInt((getEmployeeList().get(getEmployeeList().size() - 1).getId()).substring(2)) + 1;
            return String.format("E-%03d", newEmployeeId);
        }
    }

    public void btnSaveOnAction(ActionEvent actionEvent) {
        if(!isDataValid()) return;

//        ObservableList<Employee> employeeList = tblEmployee.getItems();
        List<Employee> employeeList = getEmployeeList();

        for (Employee employee : employeeList) {
            if(tblEmployee.getSelectionModel().getSelectedItem() == employee) continue;
            if(txtContact.getText().strip().equals(employee.getContact())) {
                new Alert(Alert.AlertType.ERROR, "Contact number is already exits!").show();
                txtContact.selectAll();
                txtContact.requestFocus();
                return;
            }
        }
        if(tblEmployee.getSelectionModel().isEmpty()) {
            Employee employee = new Employee(txtId.getText().strip(), txtName.getText().strip(), txtContact.getText().strip());
            employeeList.add(employee);
            btnNew.fire();
        } else {
            Employee selectedEmployee = tblEmployee.getSelectionModel().getSelectedItem();
            selectedEmployee.setName(txtName.getText().strip());
            selectedEmployee.setContact(txtContact.getText().strip());
            tblEmployee.refresh();
            btnNew.fire();
        }
    }

    private boolean isDataValid() {
        if(!txtName.getText().strip().matches("^[A-Za-z ]+$")){
            txtName.requestFocus();
            txtName.selectAll();
            return false;
        } else if (!txtContact.getText().strip().matches("\\d{3}-\\d{7}")) {
            txtContact.requestFocus();
            txtContact.selectAll();
        }
        return true;
    }

    public void btnDeleteOnAction(ActionEvent actionEvent) {
        Employee selectedEmployee = tblEmployee.getSelectionModel().getSelectedItem();
        getEmployeeList().remove(selectedEmployee);
        if (getEmployeeList().isEmpty()) btnNew.fire();
    }

    public List<Employee> getEmployeeList() {
        return tblEmployee.getItems();
    }
}
