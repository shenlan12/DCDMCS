package gui;

import starter.Starter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Project: DCDMC
 * Package: gui
 * Date: 21/Mar/2015
 * Time: 21:14
 * System Time: 9:14 PM
 */
public class DCDMCGUI extends JFrame{

    private static final Logger LOGGER = Logger.getLogger(DCDMCGUI.class.getName());

    private JPanel DCDMCPanel;
    private JTextField clusterNumberTextField;
    private JTextField similarityThresholdTextField;
    private JRadioButton hierarchicalClusteringRadioButton;
    private JRadioButton KMeansClusteringRadioButton;
    private JRadioButton deviatedDynamicTimeWarpingRadioButton;
    private JRadioButton originalDynamicTimeWarpingRadioButton;
    private JRadioButton matlabDynamicTimeWarpingRadioButton;
    private JRadioButton sakoeChibaDynamicTimeRadioButton;
    private JRadioButton itakuraParallelogramDynamicTimeRadioButton;
    private JRadioButton fastOptimalDynamicTimeRadioButton;
    private JRadioButton webUserNavigationBehaviorRadioButton;
    private JRadioButton hypnogramDatasetRadioButton;
    private JTextField otherDataSourceTextField;
    private JTextField stateNumberTextField;
    private JRadioButton markovChainModelRadioButton;
    private JRadioButton semiMarkovChainModelRadioButton;
    private JRadioButton hiddenMarkovModelRadioButton;
    private JRadioButton hiddenStateDurationMarkovRadioButton;
    private JRadioButton normalMutualInformationRadioButton;
    private JRadioButton adjustedRandIndexRadioButton;
    private JRadioButton randIndexRadioButton;
    private JRadioButton stateBasedDynamicModelRadioButton;
    private JRadioButton purityRadioButton;
    private JRadioButton hiddenSemiMarkovChainRadioButton;
    private JButton startButton;

    /**
     * class constructor
     */
    public DCDMCGUI() {
        super("Distributed Collective Dynamic Modeling & Clustering");
        setJMenuBar(createMenuBar());
        setContentPane(DCDMCPanel);
        pack();


        // initialize components
        initComponents();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLocationRelativeTo(null); // center the current frame
        setVisible(true); // show gui
        startButton.addActionListener(new ActionListener() {
            /**
             * Action performed function
             * @param e event
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> parameters = getParameters();
                setAllComponentsEnabled(false);
                Starter starter = new Starter(parameters);
//                ConsoleGUI consoleGUI = new ConsoleGUI();
                starter.runCDMC();
                setAllComponentsEnabled(true);
            }
        });
    }

    /**
     * Initialize components
     */
    private void initComponents() {

    }

    /**
     * set all components enabled
     * @param enabled true if it is enabled; otherwise not enabled
     */
    private void setAllComponentsEnabled(boolean enabled) {

        clusterNumberTextField.setEnabled(enabled);
        similarityThresholdTextField.setEnabled(enabled);
        hierarchicalClusteringRadioButton.setEnabled(enabled);
        KMeansClusteringRadioButton.setEnabled(enabled);
        deviatedDynamicTimeWarpingRadioButton.setEnabled(enabled);
        originalDynamicTimeWarpingRadioButton.setEnabled(enabled);
        matlabDynamicTimeWarpingRadioButton.setEnabled(enabled);
        sakoeChibaDynamicTimeRadioButton.setEnabled(enabled);
        itakuraParallelogramDynamicTimeRadioButton.setEnabled(enabled);
        fastOptimalDynamicTimeRadioButton.setEnabled(enabled);
        webUserNavigationBehaviorRadioButton.setEnabled(enabled);
        hypnogramDatasetRadioButton.setEnabled(enabled);
        otherDataSourceTextField.setEnabled(enabled);
        stateNumberTextField.setEnabled(enabled);
        markovChainModelRadioButton.setEnabled(enabled);
        semiMarkovChainModelRadioButton.setEnabled(enabled);
        hiddenMarkovModelRadioButton.setEnabled(enabled);
        hiddenStateDurationMarkovRadioButton.setEnabled(enabled);
        normalMutualInformationRadioButton.setEnabled(enabled);
        adjustedRandIndexRadioButton.setEnabled(enabled);
        randIndexRadioButton.setEnabled(enabled);
        stateBasedDynamicModelRadioButton.setEnabled(enabled);
        purityRadioButton.setEnabled(enabled);
        hiddenSemiMarkovChainRadioButton.setEnabled(enabled);
        startButton.setEnabled(enabled);
        DCDMCPanel.setEnabled(enabled);
    }

    /**
     * Create menu bar
     * @return a object of JMenuBar
     * https://docs.oracle.com/javase/tutorial/displayCode.html?code=https://docs.oracle.com/javase/tutorial/uiswing/examples/components/MenuDemoProject/src/components/MenuDemo.java
     */
    public JMenuBar createMenuBar() {

        JMenuBar menuBar;
        JMenu menu, submenu;
        JMenuItem menuItem;
        JRadioButtonMenuItem rbMenuItem;
        JCheckBoxMenuItem cbMenuItem;

        //Create the menu bar.
        menuBar = new JMenuBar();

        //Build the first menu.
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_A);
        menuBar.add(menu);

        //a group of JMenuItems
        menuItem = new JMenuItem("Import Config File ...", KeyEvent.VK_T);
        menu.add(menuItem);

        menu.addSeparator();

        //a submenu
        submenu = new JMenu("Data Preprocess");
        submenu.setMnemonic(KeyEvent.VK_S);

        menuItem = new JMenuItem("Calculate Distance Matrix");
        submenu.add(menuItem);
        menu.add(submenu);

        //Build second menu in the menu bar.
        menu = new JMenu("Help");
        menuBar.add(menu);
        menuItem = new JMenuItem("Version");
        menu.add(menuItem);
        menuItem = new JMenuItem("About Us");
        menu.add(menuItem);

        return menuBar;
    }


    /**
     * Get all parameter settings given the GUI
     * @return a list of parameters
     */
    private List<String> getParameters() {

        List<String> configs = new ArrayList<String>();

        // cluster number
        String clusterNumber = this.clusterNumberTextField.getText();
        configs.add(clusterNumber);

        // similarity threshold
        String similarityThreshold = this.similarityThresholdTextField.getText();
        configs.add(similarityThreshold);

        // data source + state number
        String dataSource = getDataSource();
        String stateNumber = stateNumberTextField.getText();
        configs.add(dataSource + " " + stateNumber);

        // dynamic time warping
        String dynamicTimeWarping = getDynamicTimeWarping();
        configs.add(dynamicTimeWarping);

        // stopping criteria
        String stoppingCriteria = getStoppingCriteria();
        configs.add(stoppingCriteria);

        // model type + dynamic model
        String modelType = getModelType();
        String dynamicModel = getDynamicModel();
        configs.add(modelType + " " + dynamicModel);

        return configs;
    }

    /**
     * Get data soruce
     * @return a string name of data source
     */
    private String getDataSource() {
        String res = null;
        if (hypnogramDatasetRadioButton.isSelected()) {
            res = "hypnogram" + " " + "/Users/chiyingwang/Documents/IntelliJIdeaSpace/DCDMCS/dataset/hypnogram.csv";
        } else if (webUserNavigationBehaviorRadioButton.isSelected()) {
            res = "msnbc" + " " + "/Users/chiyingwang/Documents/IntelliJIdeaSpace/DCDMCS/dataset/msnbcData.csv";
        } else {
            LOGGER.warning("The input data source is null!");
        }

        return res;
    }

    /**
     * Get dynamic time warping
     * @return a string name of dynamic time warping
     */
    private String getDynamicTimeWarping() {
        String res = null;

        if (originalDynamicTimeWarpingRadioButton.isSelected()) {
            res = "ORIGINALDTW";
        } else if (matlabDynamicTimeWarpingRadioButton.isSelected()) {
            res = "MATLABORIGINALDTW";
        } else if (sakoeChibaDynamicTimeRadioButton.isSelected()) {
            res = "SAKOECHIBADTW";
        } else if (itakuraParallelogramDynamicTimeRadioButton.isSelected()) {
            res = "ITAKURAPARALLELOGRAMDTW";
        } else if (fastOptimalDynamicTimeRadioButton.isSelected()) {
            res = "FASTOPTIMALDTW";
        } else if (deviatedDynamicTimeWarpingRadioButton.isSelected()) {
            res = "DEVIATEDDTW";
        } else {
            LOGGER.warning("The dynamic time warping is null!");
        }

        return res;
    }

    /**
     * Get stopping criteria
     * @return a string name of stopping criteria
     */
    private String getStoppingCriteria() {
        String res = null;

        if (randIndexRadioButton.isSelected()) {
            res = "RANDINDEX";
        } else if (adjustedRandIndexRadioButton.isSelected()) {
            res = "ADJUSTEDRANDINDEX";
        } else if (normalMutualInformationRadioButton.isSelected()) {
            res = "NORMALIZEDMUTUALINFORMATION";
        } else if (purityRadioButton.isSelected()) {
            res = "PURITY";
        } else {
            LOGGER.warning("The stopping criteria is null!");
        }

        return res;
    }

    /**
     * Get model type
     * @return a string name of model type
     */
    private String getModelType(){
        String res = null;
        if (stateBasedDynamicModelRadioButton.isSelected()) {
            res = "STATEBASEDDYNAMICMODELS";
        } else {
            LOGGER.warning("The model type is null!");
        }

        return res;
    }

    /**
     * Get dynamic model
     * @return a string name of dynamic model
     */
    private String getDynamicModel(){
        String res = null;
        if (markovChainModelRadioButton.isSelected()) {
            res = "MARKOVCHAINMODEL";
        } else if (semiMarkovChainModelRadioButton.isSelected()) {
            res = "SEMIMARKOVCHAINMODEL";
        } else if (hiddenMarkovModelRadioButton.isSelected()) {
            res = "HIDDENMARKOVMODEL";
        } else if (hiddenSemiMarkovChainRadioButton.isSelected()) {
            res = "";
        } else if (hiddenStateDurationMarkovRadioButton.isSelected()){
            res = "";
        } else {
            LOGGER.warning("The dynamic model is null!");
        }

        return res;
    }

    /**
     * test
     * @param args user input
     */
    public static void main(String[] args) {
        DCDMCGUI test = new DCDMCGUI();
    }

}
