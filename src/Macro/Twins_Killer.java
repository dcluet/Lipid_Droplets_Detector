macro "Twins_Killer"{

    //Remove all twins particles
    //Keep the biggest

    /*
        To increase speed I will sort the ROI by name
        The first 4 numbers are the slice.
        Thus at each iteration the program will assemble the ROI of each slice
        When the zDistance is to big I can break the twin_killer loop.

        PB: ImageJ as this format:
        slice 1: 0001
        slice 10: 00010
        ?????
        I have to rename myself first and reorder
    */

    //Retrieve arguments into an array
    Argument = getArgument();
    Arguments = split(Argument, "*");

    myStack = Arguments[0];
    nROI = parseFloat(Arguments[1]);
    it = parseFloat(Arguments[2]);
    seuil = parseFloat(Arguments[3]);
    zDistance = parseFloat(Arguments[4]);
    enlargement = parseFloat(Arguments[5]);
    CircMinC = parseFloat(Arguments[6]);
    SizeMaxC = parseFloat(Arguments[7]);
    SizeMin = parseFloat(Arguments[8]);
    CircMaxC = parseFloat(Arguments[9]);
    enhance = parseFloat(Arguments[10]);
    FPT = Arguments[11];
    myProgress = parseFloat(Arguments[12]);

    //Select the stack used to detect the particles
    selectWindow(myStack);

    //Rename and sort all found ROIs
    //Order the ROIs
    PathOR = getDirectory("macros");
    PathOR += "Droplets"+File.separator;
    PathOR += "Order_ROI.java";
    runMacro(PathOR, "" + it);

    //Process all ROIs
    for (n =0; n< roiManager("count"); n++){

        //Set the filling color to black
        setForegroundColor (0,0,0);

        //Select the Reference ROI
        roiManager("Select", n);

        //Initialize the boolean for deletion of the current ROI
        Erase=0;

        //Obtain geometrical characteristics of the particle
        List.setMeasurements;
        Xr = List.getValue("X");
        Yr = List.getValue("Y");
        Ar = List.getValue("Area");
        Cr = List.getValue("Circ.");
        Sr = getSliceNumber();

        //Determine if the minimal size threshold is applied or not
        if((it==1) && (enhance==0)){
            SizeMinl = 0;
        }else{
            SizeMinl = SizeMin;
        }

        //Delete if the current ROI is too big or too small
        if((Ar>SizeMaxC) || (Ar<SizeMinl)){
            //Clean false positive
            Erase = 1;

        }else{
            //Else compare to all other ROIs
            for(N=n+1; N<roiManager("count"); N++){

                //Prompt user of progress in the IJ main window
                message = "Iteration " + it;
                message += " ROI " + n;
                message += " out of " + roiManager("count");
                message += " Batch started " + FPT;
                showStatus(message);
                showProgress(myProgress);

                //Select the ROI to be compared
                roiManager("Select", N);

                //Obtain geometrical characteristics of the particle
                List.setMeasurements;
                X = List.getValue("X");
                Y = List.getValue("Y");
                A = List.getValue("Area");
                C = List.getValue("Circ.");
                S = getSliceNumber();

                //If the particle is too big/small or bad circularity
                //The current ROI is deleted
                if((C<CircMinC)||(A>SizeMaxC)||(Ar<SizeMinl)||(C>CircMaxC)){
                    //Clean false positive
                    roiManager("Select", N);
                    run("Enlarge...", "enlarge=" + enlargement);
                    run("Fill", "slice");
                    roiManager("Delete");

                    //Correct the index N to take into account that one
                    //ROI was removed
                    N = N -1;

                }else{

                //If the two particles are not in the same slice and
                //are closer that the Z Threshold
                if ((abs(S-Sr)<=zDistance) && (abs(S-Sr)>=0)){

                    //Calculate the XY distance between the 2 ROIs
                    d = sqrt( (X-Xr)*(X-Xr) + (Y-Yr)*(Y-Yr) );

                    //If the distance is smaller than the minimal authorized
                    if (d<seuil){

                        //If the new ROI is bigger it become the reference one
                        //and the other will be deleted
                        if(A>=Ar){

                            //Update the reference geometrical values
                            Xr = X;
                            Yr = Y;
                            Ar = A;

                            //Order to delete the reference ROI
                            Erase = 1;
                        }

                        //If the new ROI is smaller it is deleted
                        if(A<Ar){

                            //Select the current ROI, enlarge and delete
                            roiManager("Select", N);
                            run("Enlarge...", "enlarge=" + enlargement);
                            run("Fill", "slice");
                            roiManager("Delete");

                            //Correct the index N to take into account that one
                            //ROI was removed
                            N = N -1;
                        }
                    }

                }else if(abs(S-Sr)>zDistance){
                    /*
                        If more than z slices => No need to compare this
                        one the others -> Break
                    */
                    N = 10 * roiManager("count");
                }
            }
        }
        }

        //Delete the reference ROI if required
        if(Erase==1){
            roiManager("Select", n);
            setForegroundColor (0,0,0);
            run("Fill", "slice");
            roiManager("Delete");

            //Correct the indexes N and n to take into account that one
            //ROI was removed
            n=n-1;
            N=N-1;
         }
    }

    //Select the window used to detect the particles
    selectWindow(myStack);
    setForegroundColor (0,0,0);

        //To increase the speed fill only new particles
        for(i=0; i<roiManager("count"); i++){
            roiManager("Select",i);
            myName = Roi.getName;
            if(lastIndexOf(myName, "*")==-1){

                //Fill and rename the particle
                run("Enlarge...", "enlarge=" + enlargement);
                run("Fill", "slice");
                newName =myName + "*";
                roiManager("Rename", newName);
            }
        }
}//END TWIN_KILLER
