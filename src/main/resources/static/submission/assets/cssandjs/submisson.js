async function submit_new_client() {
    let file = document.getElementById("pdf_input_form").files[0];
    let tg_nick = document.getElementById("tg_name_input_form").value;
    if (file === null || typeof file == 'undefined') {
        alert('Choose pdf file!');
        return;
    }
    if (tg_nick == null || tg_nick.valueOf() == "") {
        alert('Enter Nickname!');
        return;
    }

    const blob = new FormData();
    blob.append("blob", file);

    console.log("nick:" + tg_nick);

    await fetch("/submit_blob", {
        method: "POST",
        body: blob,
        headers: {
            tg_nick: tg_nick
        }
    });

}

