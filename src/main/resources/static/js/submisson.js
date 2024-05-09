getCVRoastingPrice();
getMentoringPrice();

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

    if (phone == null || phone.valueOf() == "") {
        alert('Enter Phone!');
        return;
    }

    const form_data = new FormData();
    form_data.append("form_data", file);

    await fetch("./pricing/cv", {
        method: "POST",
        body: form_data,
        headers: {
            'TG-NAME': tgName,
            'PHONE': phone
        }
    });
}


async function submit_new_client_mentoringSubscription() {
    let tgName = document.getElementById("tg_name_input_form_mentoring").value;
    let phone = document.getElementById("phone_input_form_mentoring").value;
    if (tgName == null || tgName.valueOf() == "") {
        alert('Enter Nickname!');
        return;
    }
    if (phone == null || phone.valueOf() == "") {
        alert('Enter Phone!');
        return;
    }

    let mentoring_data = {
        'TG-NAME': tgName,
        'PHONE': phone
    }
    await fetch("./pricing/mentoring", {
        method: "POST",
        body: JSON.stringify(mentoring_data),
        headers: {
            "Content-Type": "application/json; charset=UTF-8"
        }
    });
}

async function getCVRoastingPrice() {
    let response = await fetch("./pricing/cv/price");
    let cv_price = await response.text();
    document.getElementById("submit_button").innerHTML += `С тебя ${cv_price} тыщ`;
}

async function getMentoringPrice() {
    let response = await fetch("./pricing/mentoring/price");
    let mentoring_price = await response.text();
    document.getElementById("submit_button_mentoring").innerHTML += `С тебя ${mentoring_price} тыщ`;
}

