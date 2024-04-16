async function submit_new_client() {
    let file = document.getElementById("pdf_input_form").files[0];
    let tgName = document.getElementById("tg_name_input_form").value;
    if (file === null || typeof file == 'undefined') {
        alert('Choose pdf file!');
        return;
    }
    if (tgName == null || tgName.valueOf() == "") {
        alert('Enter Nickname!');
        return;
    }

    const blob = new FormData();
    blob.append("blob", file);

    await fetch("/resume/submit/cv_blob", {
        method: "POST",
        body: blob,
        headers: {
            tg_name: tgName
        }
    });

}

