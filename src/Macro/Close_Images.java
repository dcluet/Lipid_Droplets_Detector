macro "Close_Images"{
    while (nImages>0) {
        selectImage(nImages);
        close();
    }
}//END Macro
