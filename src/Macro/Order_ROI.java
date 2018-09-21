macro "Order_ROI"{

    //Retrieve the current iteration
    it = parseFloat(getArgument);

    //Only rename new ones
    for (roi=0; roi<roiManager("count"); roi++){

        //Select the current ROI
        roiManager("Select", roi);

        //Get the ROI name
        myName = Roi.getName;

        //If the name was not previously manipulated
        if(lastIndexOf(myName, "_")==-1){

            //Get the slice number
            S = getSliceNumber();

            //Add a 0 prefix for slice number <10
            if (S>=10){
                Prefix = "";
            }else if(S<10){
                Prefix = "0";
            }

            //Add prefix for particle number (to have the same string length)
            PrefixRoi = "";
            if (roi<10000){
                PrefixRoi += "0";
            }
            if (roi<1000){
                PrefixRoi += "0";
            }
            if (roi<100){
                PrefixRoi += "0";
            }
            if (roi<10){
                PrefixRoi += "0";
            }

            //Rename the ROI
            newName = Prefix + S + "_" + it + "_" + PrefixRoi + roi;
            roiManager("Rename", newName);
        }
    }
    //Sort the ROI first using its slice, then its number
    roiManager("sort");

}
