macro "Close_Images"{

    //Close all opened images 

    while (nImages>0) {
        selectImage(nImages);
        close();
    }
}//END Macro
