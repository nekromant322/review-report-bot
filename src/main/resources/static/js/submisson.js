async function submit_new_client() {
    // let offer_accepted = document.getElementById("offer_checkbox")
    // if (!offer_accepted.checked) {
    //     alert("Read and accept public offer!");
    //     return;
    // }
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

    const form_data = new FormData();
    form_data.append("form_data", file);

    await fetch("../pricing", {
        method: "POST",
        body: form_data,
        headers: {
            tg_name: tgName
        }
    });

}

async function show_public_offer() {
    await fetch("../getoffer").then(res => res.blob())
        .then(blob => {
            var file = window.URL.createObjectURL(blob);
            window.location.assign(file);
        })
}