macro "Order_ROI"{

//Get arguments
Argument = getArgument();
Arguments = split(Argument,"\t");

it = parseFloat(Arguments[0]);

    for (roi=0; roi<roiManager("count"); roi++){
        
        //Only rename new ones
        roiManager("Select", roi);
        myName = Roi.getName;
        if(lastIndexOf(myName, "_")==-1){
            S = getSliceNumber();
            if (S>=10){
                Prefix = "";
            }else if(S<10){
                Prefix = "0";
            }

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

            newName = Prefix + S + "_" + it + "_" + PrefixRoi + roi;
            roiManager("Rename", newName);
        }
    }
    roiManager("sort");
}//END MACRO
