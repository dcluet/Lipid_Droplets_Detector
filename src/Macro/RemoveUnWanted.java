macro "RemoveUnWanted"{

    //Delete all unwanted ROI (non present in tissue)

    //Retrieve Arguments
    Argument = getArgument();
    Arguments = split(Argument, "*");

    MyWindow = Arguments[0];
    enlargement = parseFloat(Arguments[1]);

    //Analyze all ROI
    for (p=0; p<roiManager("count"); p++){

        //Set the filling color to black
        setForegroundColor(0, 0, 0);

        //Select the reference window
        selectWindow(MyWindow);

        //Select the current ROI
        roiManager("Select", p);

        //Measure the mean grey value of the ROI on the reference window
        List.setMeasurements;
        M = List.getValue("Mean");

        //If the mean if not 0
        //(== the ROI is not in the black shape of the tissue)

        if (M!=0){

            //Select window used to detect the particules of interest
            selectWindow("Raw");

            //Select the current ROI
            roiManager("Select", p);

            //Correct the ROI to ensure elimination of all signal
            //As we used max-entropy (not all signal is identified)
            run("Enlarge...", "enlarge=" + enlargement);

            //Replace the ROI shape by black
            run("Fill", "slice");

            //Delete the ROI
            roiManager("Delete");

            //Correct the index p as one ROI has been removed
            p = p -1;
        }
    }
}//END Macro
