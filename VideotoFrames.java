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

/*
 * Extracts all the frames from the video and stores them in a file in the same folder
 * as this project
 */
public class VideotoFrames
{
  
  
  // UNCOMMENT THIS METHOD TO CUT THE VIDEO INTO SEPERATE FRAMES - 
  // CURRENTLY COMMENTED OUT BECAUSE FRAMES HAVE ALREADY BEEN EXTRACTED AND STORED
	
  /*
  public static void main(String[] args)
  {
	 
	 
	// Instruction for the FFMPEG exectuable file located in the same folder as this java file  
	// description of the instruction is down below
	String videoInstruction = "ffmpeg -i 20020924_juve_dk_02a.avi VideoFrames/frame%d.jpg";
	
	try {
		// call the exectuable file "FFMPEG" which is located in the same folder as this project - 
		// this instruction is given to the exectuable and tells it to intake the video and seperate it 
		// into frames and store them in the "VideoFrames" folder
		Process p = Runtime.getRuntime().exec(videoInstruction);
	} catch (IOException e1) {
		System.out.println("FFmpeg could not execute");
		e1.printStackTrace();
	} 
	
  }

*/
  
}
