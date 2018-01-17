macro "Lipid_Droplets"{

//Key parameters
seuil = 5;
Iterations = 5;
SizeMin = 20;
SizeMax = 2000;
zDistance = 5;
enlargement = 5; //3 thus far
//Clean roiManager
roiManager("reset");

//Choose image file and open it
Path = File.openDialog("Choose file");
open(Path);

//Remove the non pixel unit
run("Properties...", "channels=1 slices="+nSlices()+" frames=1 unit=pixel pixel_width=1 pixel_height=1 voxel_depth=1.0000000");
//Get the size of the current image
W = getWidth();
H = getHeight();
T = getTitle();

//Duplication of the image
makeRectangle(0,0,W,H);
run("Duplicate...", "title=Raw duplicate");

//Create the Crop movie
waitForUser("Set on the starting slice");
Sstart = getSliceNumber();
waitForUser("Set on the ending slice");
Send = getSliceNumber();

selectWindow("Raw");
MontageFilm(Sstart, Send);

//Draw Neuropil
selectWindow("Raw");
run("Enhance Contrast", "saturated=0.35");
waitForUser("Draw the neuropil");
getSelectionCoordinates(NeuroPilX, NeuroPilY);
resetMinAndMax();
roiManager("reset");

/*
    Close everything
    strat setbatchMode
    Reopen and redo
*/

selectWindow(T);
run("Close");
selectWindow("Raw");
run("Close");
//Clean roiManager
selectWindow("ROI Manager");
run("Close");

setBatchMode(true)
open(Path);

//Remove the non pixel unit
run("Properties...", "channels=1 slices="+nSlices()+" frames=1 unit=pixel pixel_width=1 pixel_height=1 voxel_depth=1.0000000");
//Get the size of the current image
W = getWidth();
H = getHeight();
T = getTitle();

makeRectangle(0,0,W,H);
run("Duplicate...", "title=Raw duplicate");
selectWindow("Raw");
MontageFilm(Sstart, Send);

//Prepare Report
run("Duplicate...", "title=Report duplicate");
run("RGB Color");



//Process the image to detect the lipid droplets
selectWindow("Raw");
makeRectangle(0,0,W,H);
run("Gaussian Blur...", "sigma=1 stack");
run("Maximum...", "radius=5 stack");

nROI = 0;

//Iteratively detect the strongest particles and remove them
for (it=0; it<Iterations; it++){
    selectWindow("Raw");
    makeRectangle(0,0,W,H);
    run("Duplicate...", "title=Temp duplicate");
    run("Convert to Mask", "method=MaxEntropy background=Dark calculate");
    run("Analyze Particles...", "size="+SizeMin+"-"+SizeMax+" add stack");

    //I removed exclude to allow detection of ugly stuff masking true signal
    // -> to clean later
    //run("Analyze Particles...", "size="+SizeMin+"-"+SizeMax+" circularity=0.30-1.00 add stack");
    run("Close");
    if (nROI<roiManager("count")){
    selectWindow("Raw");
	for (n =0; n< roiManager("count"); n++){
        setForegroundColor (0,0,0);
		roiManager("Select", n);
		Erase=0;
		List.setMeasurements;
		Xr = List.getValue("X");
		Yr = List.getValue("Y");
		Ar = List.getValue("Area");
        Sr = getSliceNumber();

			for(N=n+1; N<roiManager("count"); N++){
                message = "Iteration " + it;
                message += " ROI " + n;
                message += " vs ROI " + N;
                message += " out of " + roiManager("count");
                message += " last score " + nROI;
                showStatus(message);
				roiManager("Select", N);
				List.setMeasurements;
				X = List.getValue("X");
				Y = List.getValue("Y");
				A = List.getValue("Area");
                S = getSliceNumber();
                if ((abs(S-Sr)<=zDistance) && (abs(S-Sr)>0)){
    				d = sqrt( (X-Xr)*(X-Xr) + (Y-Yr)*(Y-Yr) );
    				if (d<seuil){
    					if(A>Ar){
    					Xr = X;
    					Yr = Y;
    					Ar = A;
    					Erase = 1;
    					}
    					if(A<Ar){
    					roiManager("Select", N);
                        run("Enlarge...", "enlarge=" + enlargement);
    					run("Fill", "slice");
    					roiManager("Delete");
    					N = N -1;
    					}
    				}
                }
			}
        //Delete the detected particle from the image
        //If ok still in the Manager else it has to be deleted anyway
        roiManager("Select", n);
        run("Enlarge...", "enlarge=" + enlargement);
        run("Fill", "slice");
        if(Erase==1){
            roiManager("Select", n);
            //run("Fill", "slice");
            roiManager("Delete");
            n=n-1;
            N=N-1;
           }

   }
   nROI = roiManager("count");
   //Rename the ROI
   selectWindow("Raw");
   setForegroundColor (0,0,0);
		for(i=0; i<roiManager("count"); i++){
			roiManager("Select",i);
			myName = Roi.getName;
            X = List.getValue("X");
            Y = List.getValue("Y");
            if(lastIndexOf(myName, "_")==-1){
                roiManager("Rename",""+it+"_"+i);
                }
		}
    }
//waitForUser("Fin iteration n°: " +  it);
}// End of Iterations
newImage("Neuropil", "8-bit white", 2048, 2048, 1);
makeSelection("polygon", NeuroPilX, NeuroPilY);
setForegroundColor (0,0,0);
run("Fill");
for(i=0; i<roiManager("count"); i++){
    selectWindow("Neuropil");
    roiManager("Select",i);
    myName = Roi.getName;
    getStatistics(area, mean);
    if (mean<255){
        roiManager("Select",i);
        roiManager("Rename", "NP_"+myName);
        }
}

/*
    REFINE PARTICLES WITH LOCAL (ROI) VALUES?
*/

//Draw ROI on Report
selectWindow("Report");
setForegroundColor(255,0,255);

run("Line Width...", "line=3");
for(i=0; i<roiManager("count"); i++){
    roiManager("Select",i);
    run("Draw", "slice");
}
setForegroundColor(0,255,255);
makeSelection("polygon", NeuroPilX, NeuroPilY);
run("Draw", "stack");
saveAs("Tiff", Path + "_report.tif");
run("Close");

//Create the result files (to be tested)
selectWindow("Raw");
makeSelection("polygon", NeuroPilX, NeuroPilY);
roiManager("Add");
roiManager("Select", roiManager("count")-1);
roiManager("Rename", "Neuropil");
roiManager("Deselect");
roiManager("Save", Path + "_RoiSet.zip");
run("Set Measurements...", "area centroid display redirect=None decimal=3");
roiManager("Measure");
saveAs("Results", Path + "_Results.csv");
run("Clear Results");


//Close all non required images.
selectWindow("Neuropil");
run("Close");

selectWindow("Raw");
run("Close");

selectWindow(T);
run("Close");

waitForUser("Analysis is over")


function MontageFilm(Sstart, Send){
	N=nSlices+1;

	for (i=Send+1; i <N; i++){		//Delete all frames present after the ending frame
	setSlice(Send+1);
	run("Delete Slice");
	}
	for (i=1; i <Sstart; i++){		//Delete all frames present before the starting one
	setSlice(1);
	run("Delete Slice");
	}
}

}//END MACRO
