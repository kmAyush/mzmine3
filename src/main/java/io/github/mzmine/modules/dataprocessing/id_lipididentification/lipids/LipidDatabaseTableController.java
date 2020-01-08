package io.github.mzmine.modules.dataprocessing.id_lipididentification.lipids;

import java.awt.Color;
import java.text.NumberFormat;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import io.github.mzmine.datamodel.IonizationType;
import io.github.mzmine.gui.chartbasics.gui.javafx.EChartViewer;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.LipidSearchParameters;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.lipids.lipidmodifications.LipidModification;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.lipidutils.LipidIdentity;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.parameters.parametertypes.tolerances.MZTolerance;
import io.github.mzmine.util.FormulaUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

public class LipidDatabaseTableController {

  @FXML
  private TableView<TableModel> lipidDatabaseTableView;

  @FXML
  private TableColumn<TableModel, String> idColumn;

  @FXML
  private TableColumn<TableModel, String> lipidCoreClassColumn;

  @FXML
  private TableColumn<TableModel, String> lipidMainClassColumn;

  @FXML
  private TableColumn<TableModel, String> lipidClassColumn;

  @FXML
  private TableColumn<TableModel, String> formulaColumn;

  @FXML
  private TableColumn<TableModel, String> abbreviationColumn;

  @FXML
  private TableColumn<TableModel, String> ionizationColumn;

  @FXML
  private TableColumn<TableModel, String> exactMassColumn;

  @FXML
  private TableColumn<TableModel, String> infoColumn;

  @FXML
  private TableColumn<TableModel, String> statusColumn;

  @FXML
  private TableColumn<TableModel, String> fragmentsPosColumn;

  @FXML
  private TableColumn<TableModel, String> fragmentsNegColumn;

  @FXML
  private BorderPane kendrickPlotPanelCH2;

  @FXML
  private BorderPane kendrickPlotPanelH;

  ObservableList<TableModel> tableData = FXCollections.observableArrayList();

  private int minChainLength;
  private int maxChainLength;
  private int minDoubleBonds;
  private int maxDoubleBonds;
  private IonizationType ionizationType;
  private boolean useModification;
  private LipidModification[] lipidModification;
  private MZTolerance mzTolerance;

  public void initialize(ParameterSet parameters, LipidClasses[] selectedLipids) {

    this.minChainLength =
        parameters.getParameter(LipidSearchParameters.chainLength).getValue().lowerEndpoint();
    this.maxChainLength =
        parameters.getParameter(LipidSearchParameters.chainLength).getValue().upperEndpoint();
    this.minDoubleBonds =
        parameters.getParameter(LipidSearchParameters.doubleBonds).getValue().lowerEndpoint();
    this.maxDoubleBonds =
        parameters.getParameter(LipidSearchParameters.doubleBonds).getValue().upperEndpoint();
    this.ionizationType =
        parameters.getParameter(LipidSearchParameters.ionizationMethod).getValue();
    this.useModification =
        parameters.getParameter(LipidSearchParameters.searchForModifications).getValue();
    if (useModification) {
      this.lipidModification = parameters.getParameter(LipidSearchParameters.searchForModifications)
          .getEmbeddedParameters().getParameter(LipidSearchModificationsParamters.modification)
          .getValue();
    }
    this.mzTolerance = parameters.getParameter(LipidSearchParameters.mzTolerance).getValue();

    NumberFormat numberFormat = MZmineCore.getConfiguration().getMZFormat();
    int id = 1;

    for (int i = 0; i < selectedLipids.length; i++) {
      int numberOfAcylChains = selectedLipids[i].getNumberOfAcylChains();
      int numberOfAlkylChains = selectedLipids[i].getNumberofAlkyChains();
      for (int chainLength = minChainLength; chainLength <= maxChainLength; chainLength++) {
        for (int chainDoubleBonds =
            minDoubleBonds; chainDoubleBonds <= maxDoubleBonds; chainDoubleBonds++) {

          // If we have non-zero fatty acid, which is shorter
          // than minimal length, skip this lipid
          if (((chainLength > 0) && (chainLength < minChainLength))) {
            continue;
          }

          // If we have more double bonds than carbons, it
          // doesn't make sense, so let's skip such lipids
          if (((chainDoubleBonds > 0) && (chainDoubleBonds > chainLength - 1))) {
            continue;
          }
          // Prepare a lipid instance
          LipidIdentity lipidChain = new LipidIdentity(selectedLipids[i], chainLength,
              chainDoubleBonds, numberOfAcylChains, numberOfAlkylChains);

          tableData.add(new TableModel(String.valueOf(id), // id
              selectedLipids[i].getCoreClass().getName(), // core class
              selectedLipids[i].getMainClass().getName(), // main class
              selectedLipids[i].getName(), // lipid class
              lipidChain.getFormula(), // molecular formula
              selectedLipids[i].getAbbr() + " (" + chainLength + ":" + chainDoubleBonds + ")", // abbr
              ionizationType.toString(), // ionization type
              numberFormat.format(lipidChain.getMass() + ionizationType.getAddedMass()), // exact
                                                                                         // mass
              "", // info
              "", // status
              String.join(", ", selectedLipids[i].getMsmsFragmentsPositiveIonization()), // msms
                                                                                         // fragments
                                                                                         // postive
              String.join(", ", selectedLipids[i].getMsmsFragmentsNegativeIonization()))); // msms
                                                                                           // fragments
                                                                                           // negative
          id++;
          if (useModification) {
            for (int j = 0; j < lipidModification.length; j++) {
              tableData.add(new TableModel(String.valueOf(id), // id
                  selectedLipids[i].getCoreClass().getName(), // core class
                  selectedLipids[i].getMainClass().getName(), // main class
                  selectedLipids[i].getName() + " " + lipidModification[j].toString(), // lipid
                  // class
                  lipidChain.getFormula() + lipidModification[j].getLipidModificatio(), // sum
                  // formula
                  selectedLipids[i].getAbbr() + " (" + chainLength + ":" + chainDoubleBonds + ")"// abbr
                      + lipidModification[j].getLipidModificatio(),
                  ionizationType.toString(), // ionization type
                  numberFormat.format(lipidChain.getMass() + ionizationType.getAddedMass() // exact
                  // mass
                      + lipidModification[j].getModificationMass()),
                  "", // info
                  "", // status
                  "", // msms fragments postive
                  "")); // msms fragments negative
              id++;
            }
          }
        }
      }
    }

    idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
    lipidCoreClassColumn.setCellValueFactory(new PropertyValueFactory<>("lipidCoreClass"));
    lipidMainClassColumn.setCellValueFactory(new PropertyValueFactory<>("lipidMainClass"));
    lipidClassColumn.setCellValueFactory(new PropertyValueFactory<>("lipidClass"));
    formulaColumn.setCellValueFactory(new PropertyValueFactory<>("molecularFormula"));
    abbreviationColumn.setCellValueFactory(new PropertyValueFactory<>("abbreviation"));
    ionizationColumn.setCellValueFactory(new PropertyValueFactory<>("ionization"));
    exactMassColumn.setCellValueFactory(new PropertyValueFactory<>("exactMass"));
    infoColumn.setCellValueFactory(new PropertyValueFactory<>("info"));
    statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    fragmentsPosColumn.setCellValueFactory(new PropertyValueFactory<>("msmsFragmentsPos"));
    fragmentsNegColumn.setCellValueFactory(new PropertyValueFactory<>("msmsFragmentsNeg"));

    // check for interferences
    checkInterferences();

    // create cell factory
    statusColumn.setCellFactory(e -> new TableCell<TableModel, String>() {
      @Override
      public void updateItem(String item, boolean empty) {
        // Always invoke super constructor.
        super.updateItem(item, empty);
        if (tableData.get(getIndex() + 1).getInfo().toString().contains("Possible interference")) {
          this.setStyle("-fx-background-color:yellow;");
        } else if (tableData.get(getIndex() + 1).getInfo().contains("Interference")) {
          this.setStyle("-fx-background-color:red;");
        } else {
          this.setStyle("-fx-background-color:lightgreen;");
        }
      }

    });

    lipidDatabaseTableView.setItems(tableData);

    // add plots
    EChartViewer kendrickChartCH2 =
        new EChartViewer(create2DKendrickMassDatabasePlot("CH2"), true, true, true, true, false);
    kendrickPlotPanelCH2.setCenter(kendrickChartCH2);
    EChartViewer kendrickChartH =
        new EChartViewer(create2DKendrickMassDatabasePlot("H"), true, true, true, true, false);
    kendrickPlotPanelH.setCenter(kendrickChartH);
  }

  /**
   * This method checks for m/z interferences in the generated database table using the user set m/z
   * window
   */
  private void checkInterferences() {
    for (int i = 0; i < tableData.size(); i++) {
      for (int j = 0; j < tableData.size(); j++) {
        double valueOne = Double.parseDouble(tableData.get(j).getExactMass());
        double valueTwo = Double.parseDouble(tableData.get(i).getExactMass());
        if (valueOne == valueTwo && j != i) {
          tableData.get(j).setInfo("Interference with: " + tableData.get(i).getAbbreviation());
        } else if (mzTolerance.checkWithinTolerance(valueOne, valueTwo) && j != i) {
          tableData.get(j)
              .setInfo("Possible interference with: " + tableData.get(i).getAbbreviation());
        }
      }
    }
  }

  /**
   * This method creates Kendrick database plots to visualize the database and possible
   * interferences
   */
  private JFreeChart create2DKendrickMassDatabasePlot(String base) {

    XYSeriesCollection datasetCollection = new XYSeriesCollection();
    XYSeries noInterferenceSeries = new XYSeries("No interference");
    XYSeries possibleInterferenceSeries = new XYSeries("Possible interference");
    XYSeries interferenceSeries = new XYSeries("Isomeric interference");

    // add data to all series
    double yValue = 0;
    double xValue = 0;

    for (int i = 0; i < tableData.size(); i++) {

      // calc y value depending on KMD base
      if (base.equals("CH2")) {
        double exactMassFormula = FormulaUtils.calculateExactMass("CH2");
        yValue =
            ((int) (Double.parseDouble(tableData.get(i).getExactMass()) * (14 / exactMassFormula)
                + 1))
                - Double.parseDouble(tableData.get(i).getExactMass()) * (14 / exactMassFormula);
      } else if (base.equals("H")) {
        double exactMassFormula = FormulaUtils.calculateExactMass("H");
        yValue =
            ((int) (Double.parseDouble(tableData.get(i).getExactMass()) * (1 / exactMassFormula)
                + 1))
                - Double.parseDouble(tableData.get(i).getExactMass()) * (1 / exactMassFormula);
      } else {
        yValue = 0;
      }

      // get x value from table
      xValue = Double.parseDouble(tableData.get(i).getExactMass());

      // add xy values to series based on interference status
      if (tableData.get(i).getInfo().toString().contains("Possible interference")) {
        possibleInterferenceSeries.add(xValue, yValue);
      } else if (tableData.get(i).getInfo().toString().contains("Interference")) {
        interferenceSeries.add(xValue, yValue);
      } else {
        noInterferenceSeries.add(xValue, yValue);
      }
    }

    datasetCollection.addSeries(noInterferenceSeries);
    datasetCollection.addSeries(possibleInterferenceSeries);
    datasetCollection.addSeries(interferenceSeries);

    // create chart
    JFreeChart chart = ChartFactory.createScatterPlot("Database plot KMD base " + base, "m/z",
        "KMD (" + base + ")", datasetCollection, PlotOrientation.VERTICAL, true, true, false);

    chart.setBackgroundPaint(null);
    chart.getLegend().setBackgroundPaint(null);
    // create plot
    XYPlot plot = (XYPlot) chart.getPlot();
    plot.setBackgroundPaint(Color.BLACK);
    plot.setRangeGridlinesVisible(false);
    plot.setDomainGridlinesVisible(false);

    // set axis
    NumberAxis range = (NumberAxis) plot.getRangeAxis();
    range.setRange(0, 1);

    // set renderer
    XYDotRenderer renderer = new XYDotRenderer();
    renderer.setSeriesPaint(0, Color.GREEN);
    renderer.setSeriesPaint(1, Color.YELLOW);
    renderer.setSeriesPaint(2, Color.RED);
    renderer.setDotHeight(3);
    renderer.setDotWidth(3);
    plot.setRenderer(renderer);

    return chart;
  }
}
