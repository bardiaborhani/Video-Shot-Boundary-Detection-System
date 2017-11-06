
/* 
  Project 4
  Bardia Borhani
  6/1/17
  Last day of modification: 6/8/17
  
  Description: Creating a Video Shot Boundary Detection System.
  Algorithm used: Twin-comparison based approach
  Finding cuts and gradual transitions in the videos to detect where new shot start
  The GUI will give the user the option to watch different shots in the video
  FFMPEG library is used to cut the video into images - the images are stored in the same
  file that this project is stored in - in a folder called "VideoFrames" - the thousands
  of frames are stored here
  
  Assumptions:
  A video file is provided
  FFMPEG executable file is within the same folder as this java file
*/


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.*;

public class CBVR extends JFrame {

	// container to hold the video on the top left of the JFrame window
	// JLabal: set area in a JFrame to display short text string or image, or
	// both
	private JLabel photographLabel = new JLabel();

	// creates an array of JButtons used to begin each shot
	// JButton: Implementation of a "push" button - a button to press in the
	// frame
	private JButton[] button;

	// keeps order of the shot buttons - example: button[1] holds the button "shot #1" to display the first shot
	private int[] shotOrder = new int[101];
	//private ArrayList<Integer> shotOrder = new ArrayList<Integer>(101);
	
	
	// GridLayout: Layout manager that lays out a container's components in a
	// rectangular grid
	// the container is divided into equal-sized rectangles and one component is
	// placed into
	// each rectangle - example: buttons are placed into the rectangles
	private GridLayout gridLayout1;
	private GridLayout gridLayout2;
	private GridLayout gridLayout3;
	private GridLayout gridLayout4;

	// JPanel: Generic lightweight container -- similar to JFrame - where
	// components go
	private JPanel panelBottom1;
	private JPanel panelBottom2;
	private JPanel panelTop;
	private JPanel buttonPanel;

	// read the intensity values of each 4000 frames from the intensity.txt file and store them in this matrix
	private double[][] intensityMatrix = new double[5000][26];
	 
	int picNo = 0;
	int imageCount = 1; // keeps up with the number of images displayed since
						// the first page.
	int pageNo = 1;

	// last index is 4998 which represents the second to last image - this index will hold the 
	// frame-to-frame difference of frame 4998 and 4999
	private double[] sD = new double[4999]; 
	double averageSD = 0;
	
	//private double[] averageIntensityBins = new double[26];
	
	// Thresholds
	// calculated in the setThresholds method
	double tB; // frames above this value are considered the first frame of a cut
	double tS; // frames above this value and below tB are considered potential gradual transitions
	int tor;	// used to help determine the potential end of a gradual transition


	
	// MAIN METHOD
	public static void main(String args[]) {

		// dont undestand these bottom lines - passing in parameter
		// that is creating a new object that has a method??
		// SwingUtilities is a class that is imported from the JDK
		// just the thing needed to write to pull up the GUI
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				// create new CBIR object - this class is the CBIR class
				// think of the main method as seperate from this class
				CBVR app = new CBVR();

				// JFrames always need to be manually set to true to be visible
				app.setVisible(true);
			}
		});
	}

	public CBVR() {
		// The following lines set up the interface including the layout of the
		// buttons and JPanels.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // makes the close
														// button close the
														// window
		// the title is displayed at the top of the window
		setTitle("Icon Demo: Please Select a Shot Button");
		panelBottom1 = new JPanel();
		panelBottom2 = new JPanel();
		panelTop = new JPanel();
		
		// the part of the window that holds the "next page", "previous page", "intensity", and "Color Code" buttons
		buttonPanel = new JPanel();
	
		gridLayout1 = new GridLayout(4, 5, 5, 5);
		gridLayout2 = new GridLayout(2, 1, 5, 5);
		gridLayout3 = new GridLayout(1, 2, 5, 5);
		gridLayout4 = new GridLayout(2, 2, 2, 2);
		
		setLayout(gridLayout2);
		panelBottom1.setLayout(gridLayout1);
		panelBottom2.setLayout(gridLayout1);
		panelTop.setLayout(gridLayout3);

		// function from extended class - JFrame - adds the panels to the frame
		add(panelTop);
		add(panelBottom1);

		// photographLabel - name of the JLabel
		photographLabel.setVerticalTextPosition(JLabel.BOTTOM);
		photographLabel.setHorizontalTextPosition(JLabel.CENTER);
		photographLabel.setHorizontalAlignment(JLabel.CENTER);
		photographLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		buttonPanel.setLayout(gridLayout4);

		// these two components are added to the "panelTop" panel which is
		// located on the top part of the window
		panelTop.add(photographLabel);
		panelTop.add(buttonPanel);

		// 4 buttons are created
		JButton previousPage = new JButton("Previous Page");
		JButton nextPage = new JButton("Next Page");
		
		// Changes the 4 button colors frmo blue to green
		previousPage.setBackground(Color.ORANGE);
		nextPage.setBackground(Color.ORANGE);


		// adding the buttons to the panel - so it can be displayed onto the
		// panel
		// buttons NEED to be added to the panel to be displayed
		buttonPanel.add(previousPage);
		buttonPanel.add(nextPage);
	

		// adds action listener to the buttons --- tells the button what to do
		// when pressed
		nextPage.addActionListener(new nextPageHandler());
		previousPage.addActionListener(new previousPageHandler());
		setSize(1100, 750);
		// this centers the frame on the screen
		setLocationRelativeTo(null);


		button = new JButton[101];
		
		for(int buttonNum = 1; buttonNum < 100; buttonNum++){
			button[buttonNum] = new JButton("Shot #" + buttonNum);
			// panelBottom1.add(button[i]); // this was commented
			//button[i].addActionListener(new IconButtonHandler(i, icon));
			button[buttonNum].addActionListener(new IconButtonHandler(buttonNum));
		} 
		
		// reads the intensity values of each image from intensity.txt and stores them into intensityMatrix matrix
		readIntensityFile();

		//calculateAverageItensityBins();
		
		// Calculate frame to frame difference
		calculateSD();
		calculateAverageSD();
		
		// set tB, tS, and tor
		setThresholds();
		
		calculateCutsAndGradTransitions();
		
		// displays the first 20 pictures in the window - this is called two different times - 
		// once when the program is run and once when either the intensity or colorcode buttons are pressed
		displayFirstPage();
	}

	/*
	 * This method opens the intensity text file containing the intensity matrix
	 * with the histogram bin values for each image. The contents of the matrix
	 * are processed and stored in a two dimensional array called
	 * intensityMatrix.
	 */
	public void readIntensityFile() {
		
		Scanner read; 
		
		int intensityBin;
		
		// a line is read in the text file and put into this variable
		// example: line = "13965 8104 4781 3993 3107 2501 2300 2327 3038...."
		String line = "";
		
		// keep track of which frame's intensity bin values are being read in the while loop below
		int lineNumber = 1000; 
		
		try {
			read = new Scanner(new File("intensity.txt"));
			
			// A line in the intensity.txt (100 lines total for 100 pictures)
			// represents the intensity bins of every image
			while (read.hasNextLine()) {

				// get intensity bins of a picture
				// example: line = "13965 8104 4781 3993 3107 2501 2300 2327 3038...."
				line = read.nextLine();
				

				// value of each intensity bin is in string and is split up and put into an array of string
				// this makes the bins easy to access
				String[] imageStringIntensities = line.split(" ");

				for (intensityBin = 1; intensityBin <= 25; intensityBin++) {
					//imageIntIntensities.add(Integer.parseInt(imageStringIntensities[i]));
					
					// convert the bin values from string to int an put them into the matrix
					intensityMatrix[lineNumber][intensityBin] = Integer.parseInt(imageStringIntensities[intensityBin-1]);
					
				}

				// the image we are looking at
				lineNumber++;

			}
			
		} catch (FileNotFoundException EE) {
			// if the txt file isnt found then throw an exception
			System.out.println("The file intensity.txt does not exist");
		}

	} 

	/*
	public void calculateAverageItensityBins(){
		
		double sum = 0;
		
		for(int binNum = 1; binNum <= 25; binNum++){
			for(int imageNum = 1000; imageNum <= 4999; imageNum++){
			
				sum += intensityMatrix[imageNum][binNum];
			}
			
			// use the sum to get the average then insert into the average array
			averageIntensityBins[binNum] = sum / 4000;
			
			// sum is set to 0 again to find the sum of the next column during next loop 
			sum = 0;
		}
		
	}
	*/
	
	public void calculateSD() {
		
		for(int image = 1000; image <= 4998; image++){
			
			// Manhattan distance set to 0 again for calculating the manhattan distance of another picture
			double d = 0;
			
			for(int bins = 1; bins <= 25; bins++){
				// Manhattan distance gives us images that are most similar to this picture in terms
				// of intensity
				d += Math.abs( intensityMatrix[image][bins] - intensityMatrix[image+1][bins] );
			}
			
			// the value is placed into the distance array where all the manhatten distances are stored
			// to later be compared to when displaying the images in the window
			sD[image] = d;	
			
		}
		
		
	}
	
	/*
	 * Goes through the sD array and calculates the average value of the 4998 SD values
	 */
	public void calculateAverageSD(){
		
		//private double[] sD = new double[4999]; 
		
		double sum = 0;
		
		for(int image = 1000; image <= 4998; image++){
			
			sum += sD[image];
			
		}
		
		averageSD = sum / 3999;
		
	}
	
	/*
	 * Set the thresholds to be used to calculate if a frame is a cut or a gradual transition
	 */
	public void setThresholds(){
		double average = calculateAverage();
		double standardDeviation = calculateStandardDeviation();
		tB = average + (standardDeviation * 11);
		tS = average * 2; 
		tor = 2;
	}
	
	
	
	
	public double calculateAverage(){
		
		double average = 0;
		
		for(int image = 1000; image <= 4998; image++){
			average += sD[image];
		}
		
		average /= 4000;
		
		return average;
	}
	
	// Use standard deviation formula to find standard deviation 
		// FORMULA (in English): 
		// std = sqrt(  sigma(feature value - average feature value) / (number of images - 1)  )
		// store the standard deviation 
		// The standard deviation created at start of file -> private double[] standardDeviation = new double[90];
		public double calculateStandardDeviation(){
			
			// calculate the top half of the formula inside the square root
			double summation = 0;
			
			// part of the standard deviation formula -> "feature value - average feature value"
			double valueMinusAverage = 0;
			
			double standardDeviation = 0;
			
			// need to convert all the bin values first with the image size before doing anything else
			//double convertBinValue = 0; 
			
			// IMPORTANT: index 1 - 25 to represent the intensity bin values
			// IMPORTANT: index 26 - 89 to represent the colorCode bin values
			//for(int binNum = 1; binNum <= 25; binNum++){
			for(int imageNum = 1000; imageNum <= 4998; imageNum++){ 
				//sd = sqrt(  sigma(FEATURE VALUE - AVERAGE FEATURE VALUE) / (number of images - 1)  )
				//convertBinValue = intensityMatrix[imageNum][binNum] / imageSize[imageNum];
				//valueMinusAverage = convertBinValue - average[binNum];
				valueMinusAverage = sD[imageNum] - averageSD;
				
				// std = sqrt(  SIGMA(feature value - average feature value) / (number of images - 1) )
				// summation (sigma) is the valueMinusAverage of all the images summed up 
				summation += Math.pow(valueMinusAverage, 2); 
			}
			
			// std = SQRT(  sigma(feature value - average feature value) / (number of images - 1)  )
			//standardDeviation[binNum] = Math.sqrt(summation / 3999);
			
			standardDeviation = Math.sqrt(summation / 3999); 
			 
			return standardDeviation;
			
			// set back to 0 so it is ready to find the sum of all the images for the next feature - in the next loop
			//summation = 0;
			
			//}
			
		}
	
		
		
	public void calculateCutsAndGradTransitions(){

		// keeps track of the next index in the shotOrder array to store the first frame of the next shot
		int shotOrderCount = 1;	
		
		// these three variables are for calculating the gradual transitions
		// when a SD is above Ts then we state that that frame is a potential gradual transition frame
		// the potentialEnd frame is calulated by using the "tor" variable and checking if ther next
		// "tor" amount of frames next are below the Ts - if so that frame is the potentialEnd Frame
		int potentialStart = 0;	
		int potentialEnd = 0;
		double gradualSummation = 0;
		
		for(int image = 1000; image <= 4998; image++){
			
			// if the SD of the frame is above tB then it is a cut
			if( sD[image] >= tB ){
			
				shotOrder[shotOrderCount] = image-1;	// record this as a cut and place it into the shotOrder array to be placed as a shot button
				shotOrderCount++; 	// increase the index number by 1 to place the next shot in the following slot
			} 
			
			// if the SD of the frame is above tB and below tB then it is a potential gradual transition
			if( (sD[image] >= tS) && (sD[image] < tB) && (image <= 4996)){
				
				potentialStart = image; 
				potentialEnd = image;
				gradualSummation = 0; 
				
				// find the potentialEnd to this potentialStart
				while(true){
					
					// if the third to last frame is being looked at, that means this gradual transition has
					// no end - so just test to see if potential gradual transition is a real gradual transition
					if(image == 4997){
						
						// for a potential gradual transition to be a real fradual transition,
						// the sum of the SD values between the start and end of gradual transition
						// must be higher than tB (the threshold value indicating a cut)
						for(int i = potentialStart-1; i <= potentialEnd; i++){
							gradualSummation += sD[i]; 
						}
						
						// if the sum is indeed higher then record the gradual transition as a shot
						if(gradualSummation >= tB){
							shotOrder[shotOrderCount] = potentialStart;
							shotOrderCount++;
						} 
						
						// move onto looking at the other frames
						break;
					}
					
					// tor is set to 2 so the next tow frames after the potential end frame is checked
					/*
					int[] torNextFrames = new int[tor+1];
					boolean allTorBelowTs = true;
					for(int i = 1; i <= tor; i++){
						torNextFrames[i] = potentialEnd + i;
						if(torNextFrames[i] > tS){
							allTorBelowTs = false;
						}
					}
					*/
					
					int nextFrame = potentialEnd + 1;
					int nextNextFrame = potentialEnd + 2;
					
					if(sD[nextFrame] >= tB){
					//if(sD[torNextFrames[1]] >= tB){
						shotOrder[shotOrderCount] = nextFrame;	// record this as a cut and place it into the shotOrder array to be placed as a shot button
						//shotOrder[shotOrderCount] = torNextFrames[1];
						shotOrderCount++; 	// increase the index number by 1 to place the next shot in the following slot
						image++; // skip over this frame because we already counted it as a shot 
						break;
					}
					
					
					if( ((sD[nextFrame] < tS) && (sD[nextNextFrame] < tS)) ){
					//if( allTorBelowTs ){
						
						for(int i = potentialStart-1; i <= potentialEnd; i++){
							gradualSummation += sD[i];  
						}
						
						if(gradualSummation >= tB){
							shotOrder[shotOrderCount] = potentialStart;
							shotOrderCount++;
						}
						
						break;
					}
					
					potentialEnd++;
					image++;
					 
				}
				
			}
			
		}
		
	}
		
	
	
		
	/*
	 * This method displays the first twenty images in the panelBottom. The for
	 * loop starts at number one and gets the image number stored in the
	 * buttonOrder array and assigns the value to imageButNo. The button
	 * associated with the image is then added to panelBottom1. The for loop
	 * continues this process until twenty images are displayed in the
	 * panelBottom1
	 */
	private void displayFirstPage() {
		
		// makes sure that the first page is only the first 20 pictures that are presented and that
		// the next page button can be pressed another 4 times
		imageCount = 1;	// ADDED
		
		//int imageButNo = 0; 
		
		// remove all current pictures displayed in the window
		panelBottom1.removeAll();
		
		// print the first 20 pictures into the window - according to the buttonOrder array
		// the buttonOrder array values match the indexes when the program is first run
		// then when either the intensity or colorCode button is pressed, the buttonOrder changes
		// to put images in order of most similar in terms of intensity or colorCode
		for (int i = 1; i < 21; i++) {
			// System.out.println(button[i]);
			//imageButNo = shotOrder[i];
			//panelBottom1.add(button[imageButNo]);
			panelBottom1.add(button[i]); 
			imageCount++;
		}
		panelBottom1.revalidate();
		panelBottom1.repaint();

	}
	
	
	
	

	/*
	 * This class implements an ActionListener for each iconButton. When an icon
	 * button is clicked, the video is added to the
	 * photographLabel and the picNo is set to the image number selected and
	 * being displayed.
	 */
	private class IconButtonHandler implements ActionListener {
		
		// Will be used as the index of the shotOrder array - is used in the actionPerformed method
		// to let the method know which frame to start from and end on
		int shotOrderNumber = 0;
		
		// will be used alongside the shotOrderNumber variable to get the number of the first frame in each shot
		// the frame will be incremented while being used to display frames onto the window
		int currentFrame = 0;
		
		// the last frame to be included in the list of frame that are shown when a shot button is pressed
		int lastFrameShot = 0;
		
		// used to store the frame read and will use to output frame to window
		ImageIcon frameImage = null;
		Timer timer;
		
	
		// constructor - the paramter indicates what button number is pressed
		// each button number is associated with ther index of the shotOder
		// Example: button #4 will trigger shot #4 to display
		IconButtonHandler(int shotOrderNum){ 
			
			shotOrderNumber = shotOrderNum;
		
			// First frame to display
			currentFrame = shotOrder[shotOrderNum];
			
			// Last frame to display
			lastFrameShot = shotOrder[shotOrderNum + 1];
			
	
		}

		// this method is called when a shot button is clicked on
		public void actionPerformed(ActionEvent e) {
						
			currentFrame =  shotOrder[shotOrderNumber];
			lastFrameShot = shotOrder[shotOrderNumber + 1];
			
			// TEST  
			System.out.println(currentFrame);
			
			timer = new Timer();
			TimerTask task = new TimerTask(){
				public void run(){  
					if(currentFrame == lastFrameShot){
						timer.cancel();
					}
					frameImage = new ImageIcon(getClass().getResource("videoFrames/frame" + currentFrame + ".jpg"));
					photographLabel.setIcon(frameImage);
					currentFrame++;
				}
			};  
			
			// triggers the above run method - does a while loop on the run method until "timer.cancel()" is called
			// second and third parameter are very important - indicate the time spent between calls made to the "run" method
			// 50 milliseconds is what it is set at right now - will play frames back to back lieka video
			timer.scheduleAtFixedRate(task,10,50);
			
		}

	}
	
	
	
	

	/*
	 * This class implements an ActionListener for the nextPageButton. The last
	 * image number to be displayed is set to the current image count plus 20.
	 * If the endImage number equals 101, then the next page button does not
	 * display any new images because there are only 100 images to be displayed.
	 * The first picture on the next page is the image located in the
	 * buttonOrder array at the imageCount
	 */
	private class nextPageHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			
			// prints the next 20 pictures in the button array according to the value in the buttonOrder array
			int endImage = imageCount + 20;
			if (endImage <= 101) {
				panelBottom1.removeAll();
				for (int i = imageCount; i < endImage; i++) {
					// prints according to value in buttonOrder array - this says which picture to add
					panelBottom1.add(button[i]);
					
					imageCount++;

				}

				panelBottom1.revalidate();
				panelBottom1.repaint();
			}
		}

	}

	
	
	
	/*
	 * This class implements an ActionListener for the previousPageButton. The
	 * last image number to be displayed is set to the current image count minus
	 * 40. If the endImage number is less than 1, then the previous page button
	 * does not display any new images because the starting image is 1. The
	 * first picture on the next page is the image located in the buttonOrder
	 * array at the imageCount
	 */
	private class previousPageHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			// because the panelBottom1 shows 20 pictures at a time
			int startImage = imageCount - 40;
			int endImage = imageCount - 20;

			if (startImage >= 1) {

				// remove all the current buttons shown in the panel
				panelBottom1.removeAll();

				/*
				 * The for loop goes through the buttonOrder array starting with
				 * the startImage value and retrieves the image at that place
				 * and then adds the button to the panelBottom1.
				 */
				for (int i = startImage; i < endImage; i++) { 
					
					// display the last twenty shot buttons
					panelBottom1.add(button[i]);
					imageCount--;

				}

				panelBottom1.revalidate();
				panelBottom1.repaint();
			}
		}

	}

	
	
	
}
 