async function submit_new_client() {
    let file = document.getElementById("pdf_input_form").files[0];
    let tgName = document.getElementById("tg_name_input_form").value;
    let phone = document.getElementById("phone_input_form").value;
    if (file === null || typeof file == 'undefined') {
        alert('Choose pdf file!');
        return;
    }
    if (tgName == null || tgName.valueOf() == "") {
        alert('Enter Nickname!');
        return;
    }

    if(phone == null || phone.valueOf() == "") {
        alert('Enter Phone!');
        return;
    }

    const form_data = new FormData();
    form_data.append("form_data", file);

    await fetch("./pricing", {
        method: "POST",
        body: form_data,
        headers: {
            'TG-NAME': tgName,
            'PHONE': phone
        }
    });

}