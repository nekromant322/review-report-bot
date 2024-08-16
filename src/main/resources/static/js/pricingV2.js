const pdfInput = document.getElementById('pdf'),
    clearInput = document.getElementById('clear-input'),
    pdfLabel = document.getElementById('pdf-label'),
    burger = document.getElementById('burger'),
    headerLinks = document.getElementById('header-links'),
    linksClose = document.getElementById('links-close'),
    resumeButton = document.getElementById('resume-button'),
    resume = document.getElementById('resume'),
    mentoringButton = document.getElementById('mentoring-button'),
    mentoring = document.getElementById('mentoring'),
    callButton = document.getElementById('call-button'),
    call = document.getElementById('call'),
    popup = document.getElementById('popup'),
    popupTitle = popup.querySelector('.popup__content__header__title'),
    showUpgradeButton = document.getElementById('show-upgrade-button'),
    showMentoringButton = document.getElementById('show-mentoring-button'),
    showCallButton = document.getElementById('show-call-button'),
    popupClose = document.getElementById('popup-close'),
    form = document.getElementById('form'),
    pdfField = form.querySelector('.field_pdf');

clearInput.addEventListener('click', () => {
    try {
        pdfInput.value = null;
    } catch (ex) {}
    if (pdfInput.value) {
        pdfInput.parentNode.replaceChild(pdfInput.cloneNode(true), pdfInput);
    }
    pdfLabel.innerText = 'выберите файл';
});

pdfInput.addEventListener('change', () => {
    document.getElementById('pdf-label').innerText = pdfInput.files[0].name;
});

burger.addEventListener('click', () => {
    headerLinks.classList.toggle('header__links_active');
});

linksClose.addEventListener('click', () => {
    headerLinks.classList.remove('header__links_active');
});

resumeButton.addEventListener('click', () => {
    resume.scrollIntoView({behavior: 'smooth'});
});

mentoringButton.addEventListener('click', () => {
    mentoring.scrollIntoView({behavior: 'smooth'});
});

callButton.addEventListener('click', () => {
    call.scrollIntoView({behavior: 'smooth'});
});

showUpgradeButton.addEventListener('click', () => {
    popup.classList.remove('popup_hidden');
    popupTitle.innerText = 'Апгрейд резюме';
    pdfField.style.display = 'flex';
    pdfInput.required = true;
});

showCallButton.addEventListener('click', () => {
    popup.classList.remove('popup_hidden');
    popupTitle.innerText = 'Созвон';
    pdfField.style.display = 'none';
    pdfInput.required = false;
});

showMentoringButton.addEventListener('click', () => {
    popup.classList.remove('popup_hidden');
    popupTitle.innerText = 'Менторинг';
    pdfField.style.display = 'none';
    pdfInput.required = false;
});

popupClose.addEventListener('click', () => {
    popup.classList.add('popup_hidden');
    form.reset();
});

form.addEventListener('submit', async (event) => {
    event.preventDefault();
    if (popupTitle.innerText === 'Менторинг') {
        submit_new_client_mentoringSubscription();
    }
    popup.classList.add('popup_hidden');
    form.reset();
});

async function submit_new_client_mentoringSubscription() {
    if (popupTitle.innerText !== 'Менторинг') return;
    let tgNameInput = document.getElementById("tg");
    let tgName = tgNameInput.value;
    let phoneInput = document.getElementById("phone");
    let phone = phoneInput.value;
    let mentoringPromocodeId = localStorage.getItem("mentoring_promocode_id");

    if (tgNameInput.validity.valueMissing) {
        tgNameInput.setCustomValidity('Введите имя пользователя!')
        tgNameInput.reportValidity();
        return;
    }
    if (tgNameInput.validity.patternMismatch) {
        const toRemove = ["t.me", "https"];
        tgName = toRemove.reduce((acc, substr) => acc.replace(new RegExp(substr, 'g'), ''), tgName);
        tgName = tgName.replace(/[^\w]/g, '');

        tgNameInput.setCustomValidity('Сомнительно, но окэй');
        tgNameInput.reportValidity();
    }

    if (phoneInput.validity.valueMissing) {
        phoneInput.setCustomValidity('Введите телефон!');
        phoneInput.reportValidity();
        return;
    }
    if (phoneInput.validity.patternMismatch) {
        phoneInput.setCustomValidity("Чет не похоже на телефон.. \n Уж не ошибся ли ты часом?");
        phoneInput.reportValidity();
        return;
    }
    phone = phone.replace(/\D/g, '');
    phone = '7' + phone.substr(phone.length - 10);

    let mentoring_data = {
        'TG-NAME': tgName,
        'PHONE': phone,
        'MENTORING-PROMOCODE-ID': mentoringPromocodeId
    }

    await fetch("./pricing/mentoring", {
        method: "POST",
        body: JSON.stringify(mentoring_data),
        headers: {
            "Content-Type": "application/json; charset=UTF-8"
        }
    })
        .then(response => {
            if (response.ok) {
                return response.text();
            } else {
                throw new Error('Что-то пошло не так :(');
            }
        })
        .then(text => window.open(text).focus())
        .catch((error => {
            alert(error)
        }));
}

async function getMentoringPrice() {
    let response = await fetch("./pricing/mentoring/price");
    let mentoring_price = await response.text();
    document.getElementById("mentoring_price").innerHTML += `${mentoring_price} руб/мес`;
}

async function getRoastingPrice() {
    let response = await fetch("./pricing/roasting/price");
    let roasting_price = await response.text();
    document.getElementById("roasting_price").innerHTML += `${roasting_price} руб`;
}

async function mentoringPromocodePricing() {
    let promocodeInput = document.getElementById("promo").value;
    if (promocodeInput.length === 0) {
        alert('Что-то надо ввести');
        return;
    }

    let response = await fetch("./promocodes?text=" + promocodeInput);

    if (response.status == 404) {
        alert("Промокода с таким текстом не существует!");
        return;
    }
    let mentoring_promocode = await response.json();
    if (!mentoring_promocode.active || mentoring_promocode.maxUsesNumber <= mentoring_promocode.counterUsed) {
        alert("Этот промокод недоступен!\nПопробуйте другой...");
        return;
    }

    if (!await checkPromocodeCompatibility("MENTORING", mentoring_promocode.id)) {
        alert('Промокод не соответствует желаемой услуге!');
        return;
    }

    localStorage.setItem("mentoring_promocode_id", mentoring_promocode.id);
    let discountPercent = mentoring_promocode.discountPercent;

    response = await fetch("./pricing/mentoring/price");
    let mentoring_price = await response.text();
    let discount_price = Math.round(mentoring_price * (1 - discountPercent / 100));
    if (discount_price == 0) {
        alert("Сумма к оплате - 0 р.\nТак не должно быть. Выберите другой промокод.\nИли без него");
        window.location.reload();
        return;
    }
    document.getElementById("show-mentoring-button").innerHTML = `К оплате <s>${mentoring_price}</s> ${discount_price} р.`;
    document.getElementById("promo").disabled = true;
}

async function checkPromocodeCompatibility(serviceType, promocodeId) {
    return fetch("./servicetypes/checkpromocode?promocode_id=" + promocodeId + "&service_type=" + serviceType)
        .then(response => response.text())
        .then(text => JSON.parse(text));
}

getMentoringPrice();
getRoastingPrice();

document.getElementById("promo").addEventListener('input', () => {
    if (popupTitle.innerText === 'Апгрейд резюме') {
        roastingPromocodePricing();
    } else if (popupTitle.innerText === 'Менторинг') {
        mentoringPromocodePricing();
    }
});