<?php
$target_dir = "uploads/";

$target_file = $target_dir . basename($_FILES["fileToUpload"]["name"]);
$uploadOk = 1;
$imageFileType = pathinfo($target_file,PATHINFO_EXTENSION);
// Check if image file is a actual image or fake image
if(isset($_POST["submit"])) {

    $check = getimagesize($_FILES["fileToUpload"]["tmp_name"]);
    if($check !== false) {
        //echo "File is an image - " . $check["mime"] . ".";
        $uploadOk = 1;
    } else {
        //echo "File is not an image.";
        $uploadOk = 0;
    }
    
}

 // Check if file already exists
if (file_exists($target_file)) {
        echo "{\"message\":\"Sorry File already exists.\",\"path\":\"karthik\"}";
    $uploadOk = 0;
}
// Check file size

// Allow certain file formats
if($imageFileType != "jpg" && $imageFileType != "png" && $imageFileType != "jpeg"
&& $imageFileType != "gif" && $imageFileType != "mp4" ) {
    echo "{\"message\":\"Sorry, only JPG, JPEG, PNG & GIF files are allowed.\",\"\path\":\"karthik\"}";
    $uploadOk = 0;
}
// Check if $uploadOk is set to 0 by an error
if ($uploadOk == 0) {
    echo "{\"message\":\"Sorry, Your file is not uploaded.\",\"path\":\"karthik\"}";
// if everything is ok, try to upload file
} else {
    if (move_uploaded_file($_FILES["fileToUpload"]["tmp_name"], $target_file)) {
        echo "{\"message\":\"The file ". basename( $_FILES["fileToUpload"]["name"]). " has been uploaded.\",\"path\":\"karthik\"}";;
    } else {
        echo "{\"message\":\"Sorry, Error in uploading.\",\"path\":\"karthik\"}";
    }
}


?>