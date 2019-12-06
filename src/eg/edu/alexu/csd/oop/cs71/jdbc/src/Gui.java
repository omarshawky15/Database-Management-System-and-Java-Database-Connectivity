package eg.edu.alexu.csd.oop.cs71.jdbc.src;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;


public class Gui extends Application {

    public static void main (String[] args) {
        launch(args);
    }

    TableView<Object[]> table = new TableView<>();
    static String success="";
    @Override
    public void start(Stage stage) throws IOException {
        table.setEditable(true);
        Scene scene = new Scene(new Group());
        stage.setTitle("SQL Database");
        table.setPrefWidth(1150);
        table.setPrefHeight(550);
        //table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        stage.setWidth(1200);
        stage.setHeight(700);
        stage.setResizable(false);
        TextField textField =new TextField();
        Button button =new Button("Execute Query");
        Button addBatch =new Button("Add to Batch");
        Button executeBacth =new Button("Execute Batch");
        Button clearBacth =new Button("Clear Batch");
        Button clear =new Button("Clear");
        clear.setOnAction(e->{
            textField.clear();
            textField.requestFocus();
        });
        SQLDriver SQLDriver =new SQLDriver();
        DBLogger dbLogger=new DBLogger();
        dbLogger.addLog("finer","Driver Created");
        Properties info = new Properties();
        File dbDir = new File("");
        info.put("path", dbDir.getAbsoluteFile());
        Statement statement=null;
        Connection connection=null;
        try{
              connection = SQLDriver.connect("jdbc:xmldb://localhost", info);
            dbLogger.addLog("finer","Connection Initiated");
            statement=connection.createStatement();
            dbLogger.addLog("finer","Statement Created");

        }catch (Exception e)
        {
            success=e.getMessage();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText(success);
            alert.setContentText("Please try again!");
            success="";
            dbLogger.addLog("Severe","Connection Failed");
            alert.showAndWait();
        }
        Label rowNum=new Label();
        Statement finalStatement = statement;
        button.setOnAction(e->{
            String query=textField.getText();
            query=query.replaceAll(";","");
            Object object;
            query=query.toLowerCase();
            if(query.contains("select"))
            {
                try {
                    assert finalStatement != null;
                    object = finalStatement.executeQuery(query);
                    dbLogger.addLog("fine","Select Query executed");

                    //method to transfer result set to 2d array of Objects
                    table.getColumns().clear();
                    Resultset temp =(Resultset)object;
                    Object[][] x=temp.tableData;
                    ObservableList<Object[]> data = FXCollections.observableArrayList();
                    Object[][] y = new Object[x.length][x[0].length];
                    for(int i=0;i<x.length;i++)
                    {
                        for(int j=0;j<x[i].length;j++)
                        {
                            y[i][j]=x[i][j].toString();
                        }
                    }
                    data.addAll(Arrays.asList(y));
                    data.remove(0);
                    for (int i = 0; i < x[0].length; i++)
                    {
                        TableColumn tc = new TableColumn(x[0][i].toString());
                        tc.setMinWidth(150);
                        final int colNo = i;
                        tc.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Object[], Object>, SimpleStringProperty>() {
                            @Override
                            public SimpleStringProperty call(TableColumn.CellDataFeatures<Object[], Object> p) {
                                return new SimpleStringProperty((String) p.getValue()[colNo]);
                            }
                        });
                        table.getColumns().add(tc);

                    }
                    table.setItems(data);
                } catch (SQLException ex) {
                    success=ex.getMessage();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Error");
                    alert.setHeaderText(success);
                    alert.setContentText("Please try again!");
                    success="";
                    dbLogger.addLog("severe","Select Query failed");
                    alert.showAndWait();

                }
            }else if(query.contains("create")||query.contains("drop"))
            {
                try {
                    object = finalStatement.execute(query);
                    dbLogger.addLog("fine","Create|Drop Query executed");
                    if ((boolean) object) {
                        rowNum.setText("Success");
                    } else {
                        rowNum.setText("Fail");
                    }
                } catch (SQLException ex) {
                    success=ex.getMessage();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Error");
                    alert.setHeaderText(success);
                    alert.setContentText("Please try again!");
                    success="";
                    dbLogger.addLog("Severe","Create|Drop Query Failed!");
                    alert.showAndWait();
                }
            }else if(query.contains("update")||query.contains("insert")||query.contains("delete"))
            {
                try {
                    object = finalStatement.executeUpdate(query);
                    dbLogger.addLog("fine","Update Query executed");
                    rowNum.setText("Rows Affected: "+object.toString());
                } catch (SQLException ex) {
                    success=ex.getMessage();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Error");
                    alert.setHeaderText(success);
                    alert.setContentText("Please try again!");
                    success="";
                    dbLogger.addLog("Severe","Update Query Failed!");
                    alert.showAndWait();
                }
            }
            else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText("Invalid query");
                alert.setContentText("Please try again!");
                dbLogger.addLog("Severe","Invalid Query");
                alert.showAndWait();
            }
        });
        addBatch.setOnAction(e->{
            String query=textField.getText();
            query=query.replaceAll(";","");
            Object object;
            query=query.toLowerCase();
            try {
                assert finalStatement != null;
                finalStatement.addBatch(query);
                dbLogger.addLog("fine","Query Added to Batch");
            } catch (SQLException ex) {
                success=ex.getMessage();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText(success);
                alert.setContentText("Please try again!");
                success="";
                dbLogger.addLog("Severe","Failed to add to batch");
                alert.showAndWait();
            }
        });
        clearBacth.setOnAction(e->{
            try {
                assert finalStatement != null;
                finalStatement.clearBatch();
                dbLogger.addLog("fine","Batch cleared");

            } catch (SQLException ignored) {
            }
        });
        executeBacth.setOnAction(event -> {
            try {
                assert finalStatement != null;
                int x[]=finalStatement.executeBatch();
                dbLogger.addLog("fine","Batch Executed");
                int sum=0;
                for(int i=0;i<x.length;i++)
                    sum+=x[i];
                rowNum.setText("Rows Affected: "+sum);
            } catch (SQLException e) {
                success=e.getMessage();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText(success);
                alert.setContentText("Please try again!");
                success="";
                dbLogger.addLog("Severe","Failed to execute batch");
                alert.showAndWait();
            }
        });
        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        HBox hBox=new HBox();
        HBox buttons=new HBox();
        hBox.setSpacing(100);
        buttons.setSpacing(5);
        buttons.getChildren().addAll(button,clear,addBatch,clearBacth,executeBacth);
        hBox.getChildren().addAll(rowNum);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(hBox,textField,buttons, table);

        ((Group) scene.getRoot()).getChildren().addAll(vbox);

        stage.setScene(scene);
        stage.show();
        Connection finalConnection = connection;
        stage.setOnCloseRequest(e->{
            try {
                finalStatement.close();
                finalConnection.close();
                dbLogger.addLog("fine","Statement Closed");
                dbLogger.addLog("fine","Connection Closed");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        });
        scene.setOnKeyPressed(ke -> {
            KeyCode kc = ke.getCode();
            if(kc.equals(KeyCode.ENTER))
            {
                button.fire();
            }
        });
    }
}