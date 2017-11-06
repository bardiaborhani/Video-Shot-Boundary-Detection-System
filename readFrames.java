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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

public class readFrames {

	  // holds intensity for all frames 
	  // length is 5000 -> last index is 4999 to represent image 4999
	  // index 0 is not used so 4999 slots are made for all 4999 frames processed in this program
	  int intensityMatrix [][] = new int[5000][26];  
	
	  // keeps track of which frame is dealt with
	  int imageCount;
	  
	  public readFrames() 
	  {  
		
		// In this program we are a looking at frames #1000 - #4999 from the video file
	    for(imageCount = 1000; imageCount <= 4999; imageCount++){
	    	
	      try{	// statements that may cause a exception
	        
	    	// the line that reads the frame file
	    	BufferedImage img = ImageIO.read(new File("videoFrames/frame" + imageCount + ".jpg"));
	    	 
	    	// get width of frame
	    	int width = img.getWidth(); 
	    	 
	    	// get height of frame 
	        int height = img.getHeight();
	        
	        // These ISN'T AN ACCESSORS - it calculate the intensity
	        // the values into the matrixes (look at this class' fields)
	        // the getInternsity and getColorCode methods aren't actually accessors (like they sound like)
	        // because they return void - they are used to fill in the matrix arrays
	    	getIntensity(img, height, width);
	    	
	    	
	    	 
	      } // IOException can be thrown when reading a local file that is no longer available. 
	      catch (IOException e)	// indicated by failed input/output operations -- typically object is named "e" for exception types
	      {
	        System.out.println("Error occurred when reading the file.");
	      }
	    }
	    
	    // goes through the values in the intensityMatrix matrix and stores them in the txt file
	    writeIntensity();
	   
	    
	  }
	  
	  
	/* intensity method 
	   * All the pixels of the passed image are read and the red, green, and blue values of each pixel is retrived
	   * Using the intensity method (I = 0.299R + 0.587G + 0.114B ) the intensity of every pixel is found
	   * and the value is between 0 - 255. The bin which contains value that matches the intensity is incremented by one
	   * so that every bin holds a number representing the number of pixels whose intensity is within the proper range
	   * Pre-condition: a BufferedImage object and two int variables need to passed in (representing width and height of image)
	   * Post-condition: The values of the bins are stored in the intensityMatrix matrix
	   */
	  public void getIntensity(BufferedImage image, int height, int width){

		// When reading each pixel, these variables will hold the values of the RGB of each pixel
		int redValue;
		int greenValue;
		int blueValue;

		// Will hold the intensity value of each pixel - intensity is calculated through an equation
		double intensity;
		
		int[] pixelArray = image.getRGB(0, 0, width, height, null, 0, width); 
		
		for(int i = 0; i < pixelArray.length; i++){
			
			// grabs a pixel from the pixelArray
			Color pixel = new Color(pixelArray[i]);
			
			// values returned will be between 0 - 255
			redValue = pixel.getRed();
			greenValue = pixel.getGreen();
			blueValue = pixel.getBlue();
			
			// Intensity equation (as stated on the guidelines)
			// Intensity value will also be between 0 - 255 
			// I = 0.299R + 0.587G + 0.114B 
			intensity = (0.299 * redValue) + (0.587 * greenValue) + (0.114 * blueValue);
			
			// converting intensity variable from double to int so I can see where to place 
			// the intensity value in the array
			int intValue = (int) intensity;
			
			// finding which bin to put the intensity value in
			intValue = (intValue / 10) + 1;
			//System.out.print(intensityBins[intValue]);
			
			// the last bin is special - it contains 15 values - from 240 to 255
			if(intValue == 26){
				
				//intensityBins[intValue - 1] += 1;
				
				// put intensity of picture in the matrix that holds the intensity values of all the pictures
				intensityMatrix[imageCount][intValue - 1] += 1; 
				
			} else{
				
				// increment the values by 1 so that each bin contains a value that represents the number of pixels
				// that have intensities within the bin range
				//intensityBins[intValue] += 1;    
		
				// put intensity of picture in the matrix that holds the intensity values of all the pictures
				intensityMatrix[imageCount][intValue] += 1;
			}
		}
		
	  }
	  
	  
	//This method writes the contents of the intensity matrix to a file called intensity.txt
	  public void writeIntensity(){
		  
		try{
			
			// makes it able to print something to the text
			PrintWriter pw = new PrintWriter("intensity.txt");  
			
			// i = image
			// j = intensity bin for a image
			for(int i = 1000; i <= 4999; i++){ 
				for(int j = 1; j <= 25; j++){
					  // the values of the intensityMatrix are put into the txt file
					  // the values of the bins are written separated by whitespace
					  pw.print(intensityMatrix[i][j] + " "); 
				} 
				pw.println();
			}
			
		    pw.close();
		    
		} catch(Exception e){
			
			// throw error message if files is not found
			System.out.println("intensity.txt file doesn't exists.");
			
		}
		
	  }  
	
	public static void main(String[] args) {
		new readFrames();
	}
	
}
