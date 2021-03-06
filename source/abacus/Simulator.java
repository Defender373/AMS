package abacus;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.math.BigInteger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JCheckBox;
import javax.swing.JDialog;

import java.util.ArrayList;

public class Simulator extends JPanel implements ActionListener
{	
	private static ImageIcon resetIcon = new ImageIcon(NodeEditor.resetIm);// NodeEditor.reset;
	private static ImageIcon resetAllIcon = new ImageIcon(NodeEditor.resetAll);
	private static ImageIcon pauseIcon = new ImageIcon(NodeEditor.pause);
	private static ImageIcon playIcon = new ImageIcon(NodeEditor.play);
	private static ImageIcon playMultipleIcon = new ImageIcon(NodeEditor.playMultiple);
	private static ImageIcon stepIcon = new ImageIcon(NodeEditor.step);
	//private static ImageIcon fastForwardIcon = new ImageIcon(NodeEditor.fastForward);
	
	private int realNumSteps = 0;
	private JLabel haltLabel = new JLabel("              ");
	//private JLabel inputNumber = new JLabel("Input: 1");
	private JLabel numSteps = new JLabel("Number of Steps: 0");
	//private JLabel numRegisters = new JLabel("Number of Registers: 0");
	private JButton resetButton = new JButton("",resetIcon);
	private JButton resetAllButton = new JButton("",resetAllIcon);
	private JButton playButton  = new JButton("",playIcon);
	private JButton playMultipleButton  = new JButton("",playMultipleIcon);
	private JButton pauseButton = new JButton("",pauseIcon);
	private JButton StepButton = new JButton("",stepIcon);
	
	private String[] speeds = new String[]{"Slow","Fast", "Very Fast", "Compute"};
	private JComboBox<String> speedSelection = new JComboBox<String>(speeds);
	private JTextField tf = new JTextField(7);
	private Node curNode = null;
	private int speed = 0;
	private boolean firstClick = true;
	private boolean step = false;
	private int maxTransitions = -1;
	
	NodeEditor ne = null;
	RegisterEditor re = null;
	boolean resetMachine = false;
	boolean resetRegisters = false;
    ArrayList<Integer> intInputNums = new ArrayList<Integer>();
    ArrayList<Integer> startIntInputNums = new ArrayList<Integer>();
	
	public Simulator(NodeEditor ne, RegisterEditor re)
	{	
		this.ne = ne;
		this.re = re;
		
		resetButton.addActionListener(this);
		playButton.addActionListener(this);
		playMultipleButton.addActionListener(this);
		pauseButton.addActionListener(this);
		StepButton.addActionListener(this);
		resetAllButton.addActionListener(this);
		tf.addActionListener(this);

		setSize(480,120);
		BorderLayout bl = new BorderLayout();
		setLayout(bl);
		
		JPanel west = new JPanel();
		resetButton.setToolTipText("Reset Machine");
		west.add(resetButton);
		resetAllButton.setToolTipText("Clear Registers");
		west.add(resetAllButton);
		playButton.setToolTipText("Run Machine");
		west.add(playButton);
		playMultipleButton.setToolTipText("Run Multiple Inputs");
		west.add(playMultipleButton);
		pauseButton.setToolTipText("Pause Machine");
		west.add(pauseButton);
		StepButton.setToolTipText("Do One Step");
		west.add(StepButton);
		west.add(new JLabel("Speed:"));
		west.add(speedSelection);
		west.add(new JLabel("Maximum Transitions:"));
		west.add(tf);
		
		
		
		
		JPanel east = new JPanel();
		//east.add(inputNumber);
		//east.add(numRegisters);
		east.add(numSteps);
		
		add(west, BorderLayout.WEST);
		add(haltLabel, BorderLayout.CENTER);
		haltLabel.setForeground(Color.red);
		add(east, BorderLayout.EAST);
		
		
		Player p = new Player();
		p.start();
	}
	
//	public void setInputNumberText()
//	{
//	    if (re != null) 
//    		inputNumber.setText("Input: "+(re.regInputNum+1));
//	}
	
	public boolean runningMultiple()
	{
	    return startIntInputNums.size() > 1;
	}
	
	// begin a simulation
	public void begin()
	{	
		if (firstClick) {
			ne.clearSelection();
			re.clearSelection();
			if (!runningMultiple()){
			    intInputNums = new ArrayList<Integer>();
			    intInputNums.add(re.regInputNum);
			    startIntInputNums = new ArrayList<Integer>(intInputNums);
    			re.initial();
    	    }
	    	re.setRegisterInput(intInputNums.get(0));
	    	//setInputNumberText();
			haltLabel.setText("");
			curNode = (Node)ne.macPanel.nodes.get(0);
			firstClick = false;
			//numRegisters.setText("Number of Registers: " + ne.macPanel.getRegCount());
		}
		
		ne.lock();
		re.lock();
		String getSpeed = (String) speedSelection.getSelectedItem();
		if(getSpeed == "Slow") speed=100;
		else if(getSpeed == "Fast") speed=500;
		else if(getSpeed == "Very Fast") speed=800;
		else if(getSpeed == "Compute") speed=1000;
		try{
		    maxTransitions = Integer.parseInt(tf.getText());
		}
		catch(NumberFormatException e){
			maxTransitions = -1;
		}
		if(runningMultiple()) speed=1000;
	}
	
	public void pause()
	{
		speed = 0;
		ne.unlock();
		re.unlock();
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == resetButton)
		{
			resetMachine = true;
		}
		else if (e.getSource() == playButton)
		{
			if (ne.macPanel.nodes.size() > 0){
				begin();
		    }
			else
				JOptionPane.showMessageDialog(null,
						"You must add at least a single node to simulate a computation.");
		}
		else if (e.getSource() == playMultipleButton)
		{
			if (ne.macPanel.nodes.size() > 0)
			{
                JPanel getInputs = new JPanel();
                ArrayList<JCheckBox> inputNums = new ArrayList<JCheckBox>();
		        for (int i = 0; i < ne.getNumRegSets(); i++)
		        {
    			    JCheckBox inputNum = new JCheckBox(((Integer)(i+1)).toString());
    			    getInputs.add(inputNum);
    			    inputNums.add(inputNum);
    			}
                int result = JOptionPane.showConfirmDialog(null, getInputs, 
                    "Which inputs?", JOptionPane.OK_CANCEL_OPTION);
                boolean runOnInputs = false;
                intInputNums = new ArrayList<Integer>();
                startIntInputNums = new ArrayList<Integer>();
                while (result == JOptionPane.OK_OPTION && !runOnInputs)
                {
                    for (int i = 0; i < ne.getNumRegSets(); i++)
                    {
                        if (inputNums.get(i).isSelected())
                        {
                            intInputNums.add(i);
                            runOnInputs = true;
                        }
                    }
                    if (!runOnInputs)
                    {
                        intInputNums = new ArrayList<Integer>();
                        startIntInputNums = new ArrayList<Integer>();
                        result = JOptionPane.showConfirmDialog(null, getInputs, 
                        "Please select at least one input. Which inputs do you want to use?", JOptionPane.OK_CANCEL_OPTION);
                    }
                }
                if (runOnInputs)
                {
			        startIntInputNums = new ArrayList<Integer>(intInputNums);
                    re.initial();
                    begin();
				}
			}
			else
				JOptionPane.showMessageDialog(null,
						"You must add at least a single node to simulate computations.");
		}
		else if (e.getSource() == pauseButton)
		{
			pause();
		}
		else if (e.getSource() == resetAllButton)
		{
			resetRegisters = true;
		}
		else if (e.getSource() == StepButton)
		{
			if (ne.macPanel.nodes.size() > 0) {	
					step=true;
					ne.lock();
					re.lock();
			}
			else
				JOptionPane.showMessageDialog(null,
						"You must add at least a single node to simulate a computation.");
		}
	}
	
	class Player extends Thread
	{
		private void doStep(int halfSleep)
		{
		    boolean runbegin = false;
		    
			numSteps.setText("Number of Steps: " + ++realNumSteps);
			
			
			curNode.simSelect(Node.SELECTED_NODE);
			ne.repaint();
			
			if (halfSleep > 0)
			{
				try
				{
					Thread.sleep(halfSleep);
				}
				catch (Exception e) { }
			}
			
			curNode.simSelect(Node.SELECTED_NONE);
			// do the transition	
			Node nextNode = null;
			
			int reg = curNode.getRegister();
			
			if (curNode.isPlus())
			{
				re.addOne(reg);
				curNode.simSelect(Node.SELECTED_OUT);
				nextNode = curNode.getOut();
			}
			else
			{
				if (!re.subOne(reg))
				{ // empty
					curNode.simSelect(Node.SELECTED_OUTEMPTY);
					nextNode = curNode.getOutEmpty();
				}
				else
				{
					curNode.simSelect(Node.SELECTED_OUT);
					nextNode = curNode.getOut();
				}
			}
			
			ne.repaint();
			if (halfSleep > 0)
			{
				try
				{
					Thread.sleep(halfSleep);
				}
				catch (Exception e) { }
			}			
			
			if (nextNode == null)
			{
	    		intInputNums.remove(0);
			    if (intInputNums.size() > 0)
			    {

    			    numSteps.setText("Number of Steps: 0");
	    			haltLabel.setText("");
	    			realNumSteps = 0;
	    			try{
	    				curNode = (Node)ne.macPanel.nodes.get(0);
	    			}
	    			catch(IndexOutOfBoundsException E){
	    			}
	    			ne.clearSelection();
	    			re.clearSelection();
	    		
	    	        re.setRegisterInput(intInputNums.get(0));
	    	        //setInputNumberText();
	    	        runbegin = true;
			    }
			    else
			    {
    				haltLabel.setText("Machine Halted");
					try{
						curNode = (Node)ne.macPanel.nodes.get(0);
					}
					catch(IndexOutOfBoundsException E){
					}
					startIntInputNums = new ArrayList<Integer>();
					ne.clearSelection();
					re.clearSelection();	
					speed = 0;
				}
	    		firstClick = true;
	    		ne.unlock();
	    		re.unlock();
			}
			else
			{
				curNode.simSelect(Node.SELECTED_NONE);
			}
			
			curNode = nextNode;
			
			if (curNode != null && curNode.isPauseState())
			{ // pause!
				curNode.simSelect(Node.SELECTED_NODE);
				speed = 0;
			}
			if (runbegin)
			    begin();
			
			if (realNumSteps == maxTransitions && maxTransitions > 0 && !step){ // MAX TRANSITIONS REACHED
				//JOptionPane.showMessageDialog(null,"max transitions reached");
				intInputNums.remove(0);
			    if (intInputNums.size() > 0) // check for other input registers
			    {
    			    numSteps.setText("Number of Steps: 0");
	    			haltLabel.setText("");
	    			realNumSteps = 0;
	    			try{
	    				curNode = (Node)ne.macPanel.nodes.get(0);
	    			}
	    			catch(IndexOutOfBoundsException E){
	    			}
	    			ne.clearSelection();
	    			re.clearSelection();
	    		
	    	        re.setRegisterInput(intInputNums.get(0));
	    	        //setInputNumberText();
	    	        runbegin = true;
			    }
			    else
			    {
    				haltLabel.setText("Machine Halted");
					try{
						curNode = (Node)ne.macPanel.nodes.get(0);
					}
					catch(IndexOutOfBoundsException E){
					}
					ne.clearSelection();
					re.clearSelection();	
					speed = 0;
		    		firstClick = true;
		    		ne.unlock();
		    		re.unlock();
					haltLabel.setText("Machine Halted");
					ne.clearSelection();
					re.clearSelection();	
					speed = 0;
		    		realNumSteps = 0;
					startIntInputNums = new ArrayList<Integer>();

				}
			}
		}
		
		public void run()
		{
			while (true)
			{
				if (resetMachine || resetRegisters)
				{
					numSteps.setText("Number of Steps: 0");
					haltLabel.setText("");
					realNumSteps = 0;
					try{
						curNode = (Node)ne.macPanel.nodes.get(0);
					}
					catch(IndexOutOfBoundsException E){
					}
					//re.unlock();
					if(resetRegisters)
					{
						for(int i=1;i<10000;i++){
						re.setRegisterContents(i, BigInteger.ZERO);
						}
					}
					else if(resetMachine) re.restore(startIntInputNums);
					ne.clearSelection();
					re.clearSelection();	
					speed = 0;
					ne.unlock();
					re.unlock();
					
					intInputNums = new ArrayList<Integer>();
					startIntInputNums = new ArrayList<Integer>();
					resetMachine = false;
					resetRegisters = false;
					firstClick = true;
				}
				else if(step){
					if (firstClick) {
						ne.clearSelection();
						re.clearSelection();
						re.initial();
						haltLabel.setText("");
						curNode = (Node)ne.macPanel.nodes.get(0);
						firstClick = false;
						//numRegisters.setText("Number of Registers: " + ne.macPanel.getRegCount());
					}
					if(haltLabel.getText()!="Machine Halted"){
						doStep(100);
					}
					step=false;
				}
				else
				{				
					int val = speed;
					if (isVisible() && val > 0 && curNode != null)
					{ // slider is between 1 and 1000
						// scale it a little bit
						double squareMe = 1.0 - (val / 1000.0);
						int halfSleep = (int)(1000 * (squareMe * squareMe));
						doStep(halfSleep); // will sleep if necessary
						
					}
					{
						
						try
						{
							if(val!=1000)
								Thread.sleep(10);
						}
						catch (Exception e) { }
					}
				}
			}
		}
	}
}
