// Copyright (c) 2012, Peter C. R. Lane
// Released under Open Works License, http://owl.apotheon.org/

package jchrest.gui;

import jchrest.architecture.Chrest;
import jchrest.lib.FileUtilities;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;

public class ChrestView extends JFrame implements Observer {
  private Shell _shell;
  private Chrest _model;
  private ChrestLtmView _ltmView;
  private ChrestStmView _stmView;
  private ChrestTimeView _timeView;
  private JToolBar _toolbar;
  
  private String _lastSelectedExperimentName;
  
  //Required so that 'Experiment' sub-menu can be updated as CHREST is placed 
  //into new experiments.
  private JMenu _viewMenu; 
  private JMenu _experimentNames;
  
  //Need to store object ref here since the number of columns displayed will
  //change depending on the current value of "_model.getLearningClock()".
  private JTextField _stateAtTimeTextField; 

  public ChrestView (Chrest model) {
    this (new Shell (), model);
  }

  public ChrestView (Shell shell, Chrest model) {
    super ("CHREST Model View");
    _shell = shell;
    _model = model;
    _model.addObserver (this);
    
    _model.cloneLtm(_model.getLearningClock());
    _timeView = new ChrestTimeView (_model);
    _ltmView = new ChrestLtmView (_model, _model.getLearningClock(), ""); //This should be altered so that it displays a message informing the user that they need to select an experiment view.
    _stmView = new ChrestStmView (_model);    

    // catch close-window event
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent ev) { 
        closeView (); 
      }
    });
    createMenuBar ();

    // layout the components
    JPanel leftSide = new JPanel ();
    leftSide.setLayout (new BorderLayout ());
    leftSide.add (_timeView, BorderLayout.NORTH);
    leftSide.add (_stmView, BorderLayout.CENTER);
    
    JSplitPane jsp = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, leftSide, _ltmView);
    jsp.setOneTouchExpandable (true);
    setLayout (new BorderLayout ());
    add (jsp, BorderLayout.CENTER);
    
    setSize (550, 550);
    setVisible (true);
    
    // prompt the long-term memory to draw itself
    _ltmView.setStandardDisplay ();
  }

  private void createMenuBar () {
    JMenuBar mb = new JMenuBar ();
    mb.add (createViewMenu ());
    setJMenuBar (mb);
  }
  
  private JToolBar createToolbar(int stateAtTimeValue){
    JToolBar toolbar = new JToolBar ();
    
    //Add components to toolbar and return.
    toolbar.add(new JLabel ("<html><b>State at time:</b></html>"));
    toolbar.add(createStateAtTimeTextField(stateAtTimeValue));
    
    this._toolbar = toolbar;
    return toolbar;
  }
  
  private JTextField createStateAtTimeTextField(int stateAtTimeValue){
    JTextField stateAtTimeTextField = new JTextField(String.valueOf(stateAtTimeValue));
    Dimension d = stateAtTimeTextField.getPreferredSize();
    d.width = 120;
    stateAtTimeTextField.setPreferredSize(d);
    stateAtTimeTextField.setToolTipText("Displays STM and LTM states as they were according to the time entered (press 'ENTER' to apply filter).");
    stateAtTimeTextField.addKeyListener(new ApplyTimeFilter());
    this._stateAtTimeTextField = stateAtTimeTextField;
    return stateAtTimeTextField;
  }
  
  class ApplyTimeFilter implements KeyListener {

    @Override
    public void keyReleased(KeyEvent e) {
      if(e.getKeyCode() == KeyEvent.VK_ENTER){
        String stateAtTimeTextFieldCurrentContents = ((JTextField)e.getComponent()).getText();
        
        if(stateAtTimeTextFieldCurrentContents.matches("[0-9]+")){
          Integer stateAtTimeValue = Integer.valueOf( stateAtTimeTextFieldCurrentContents );
          _model.cloneLtm(stateAtTimeValue);
          _ltmView.update (stateAtTimeValue, true, ChrestView.this._lastSelectedExperimentName);
          _stmView.update (stateAtTimeValue, true);
          _timeView.update ();
        }
        else{
          JOptionPane.showMessageDialog(_shell,
          "Please enter positive numbers (0-9) only",
          "State at Time Error",
           JOptionPane.ERROR_MESSAGE
          );
        }
      }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}
    
  }

  public void saveLongTermMemory (File file) {
    _ltmView.saveLongTermMemory (file);
  }

  class SaveLtmAction extends AbstractAction implements ActionListener {
    private ChrestView _parent;

    public SaveLtmAction (ChrestView parent) {
      super ("Save LTM", new ImageIcon (Shell.class.getResource ("icons/SaveAs16.gif")));

      _parent = parent;
    }

    public void actionPerformed (ActionEvent e) {
      File file = FileUtilities.getSaveFilename (_shell);
      if (file != null) {
        _parent.saveLongTermMemory (file);
      }
    }
  }

  class CloseAction extends AbstractAction implements ActionListener {
    private ChrestView _view;

    public CloseAction (ChrestView view) {
      super ("Close");
      _view = view;
    }

    public void actionPerformed (ActionEvent e) {
      _view.closeView ();
    }
  }

  private JMenu createViewMenu () {
    this._viewMenu = new JMenu ("View");
    this._viewMenu.setMnemonic (KeyEvent.VK_V);
    this._viewMenu.add (this.getExperimentNamesSubMenu());
    this._viewMenu.add (new SaveLtmAction (this)).setMnemonic (KeyEvent.VK_S);
    this._viewMenu.add (new CloseAction (this)).setMnemonic (KeyEvent.VK_C);
    return this._viewMenu;
  }
  
  private JMenu getExperimentNamesSubMenu(){
    this._experimentNames = new JMenu ("Experiment");
    for(String experimentName : _model.getExperimentsLocatedInNames()){
      this._experimentNames.add(new LoadExperimentViewAction(experimentName));
    }
    return this._experimentNames;
  }
  
  class LoadExperimentViewAction extends AbstractAction implements ActionListener{
    private final String _experimentName;
    
    public LoadExperimentViewAction (String experimentName) {
      super (experimentName);
      this._experimentName = experimentName;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      ChrestView.this._lastSelectedExperimentName = _experimentName;
      _model.cloneLtm(_model.getLearningClock());
      _ltmView.update (_model.getLearningClock(), false, _experimentName);
      _stmView.update (_model.getLearningClock(), false);
      _timeView.update ();
      ChrestView.this.add (ChrestView.this.createToolbar(_model.getLearningClock()), BorderLayout.SOUTH);
    }
  }

  /** 
   * Implement the observable interface, and update the view whenever 
   * the underlying model has changed.
   */
  @Override
  public void update(Observable o, Object arg) {
    
    int positionOfExperimentsSubMenuInViewMenu = 0;
    Component[] viewMenuComponents = this._viewMenu.getMenuComponents();
    while(positionOfExperimentsSubMenuInViewMenu < viewMenuComponents.length){
      if(viewMenuComponents[positionOfExperimentsSubMenuInViewMenu].equals(this._experimentNames)){
        break;
      }
    }
    this._viewMenu.remove(this._experimentNames);
    this._viewMenu.add(this.getExperimentNamesSubMenu(), positionOfExperimentsSubMenuInViewMenu);
    
    _model.cloneLtm(_model.getLearningClock());
    _ltmView.update (_model.getLearningClock(), false, _model.getCurrentExperimentName());
    _stmView.update (_model.getLearningClock(), false);
    _timeView.update ();
  }

  /**
   * When closing the view, make sure the observer is detached from the model
   * and that cloned LTM's are cleared.
   */
  private void closeView () {
    _model.clearClonedLtm();
    _model.deleteObserver (this);
    setVisible (false);
    dispose ();
  }
}

