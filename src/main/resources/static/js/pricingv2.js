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
    pdfField = form.querySelector('.field_pdf'),
    contractLink = document.querySelector('.field_checkbox a'),
    promoInput = document.getElementById('promo'),
    promoSpinner = document.getElementById('promo-spinner'),
    promoStatus = document.getElementById('promo-status'),
    formMessage = document.getElementById('form-message');

const CV_TITLE = 'Апгрейд резюме',
    MENTORING_TITLE = 'Менторинг',
    CALL_TITLE = 'Созвон';

// ─── Inline message helpers ────────────────────────────────────

function showFormError(msg) {
    formMessage.textContent = msg;
    formMessage.className = 'form__message message-error';
}

function showFormSuccess(msg) {
    formMessage.textContent = msg;
    formMessage.className = 'form__message message-success';
}

function clearFormMessage() {
    formMessage.textContent = '';
    formMessage.className = 'form__message form__message_hidden';
}

function setFieldError(fieldId, msg) {
    const el = document.getElementById(fieldId + '-error');
    if (el) el.textContent = msg;
}

function clearFieldError(fieldId) {
    const el = document.getElementById(fieldId + '-error');
    if (el) el.textContent = '';
}

function clearAllErrors() {
    ['phone', 'name', 'tg', 'pdf'].forEach(clearFieldError);
    clearFormMessage();
}

// ─── Promo state ───────────────────────────────────────────────

let promoDebounceTimer = null;
let activePromocodeId = null;
let activePromoDiscount = 0;

function resetPromo() {
    clearTimeout(promoDebounceTimer);
    promoSpinner.classList.remove('visible');
    promoStatus.textContent = '';
    promoStatus.className = 'field__promo-status';
    activePromocodeId = null;
    activePromoDiscount = 0;
}

function setPromoSuccess(msg) {
    promoSpinner.classList.remove('visible');
    promoStatus.textContent = msg;
    promoStatus.className = 'field__promo-status status-success';
}

function setPromoError(msg) {
    promoSpinner.classList.remove('visible');
    promoStatus.textContent = msg;
    promoStatus.className = 'field__promo-status status-error';
    activePromocodeId = null;
    activePromoDiscount = 0;
}

// ─── Promo check logic ─────────────────────────────────────────

async function checkPromo() {
    const code = promoInput.value.trim();
    if (code.length === 0) {
        resetPromo();
        return;
    }

    promoSpinner.classList.add('visible');
    promoStatus.textContent = '';
    promoStatus.className = 'field__promo-status';

    let serviceType, priceEndpoint, localStorageKey;
    if (popupTitle.innerText === CV_TITLE) {
        serviceType = 'RESUME';
        priceEndpoint = './pricing/cv/price';
        localStorageKey = 'cv_promocode_id';
    } else if (popupTitle.innerText === MENTORING_TITLE) {
        serviceType = 'MENTORING_SUBSCRIBE';
        priceEndpoint = './pricing/mentoring/price';
        localStorageKey = 'mentoring_promocode_id';
    } else if (popupTitle.innerText === CALL_TITLE) {
        serviceType = 'CALL';
        priceEndpoint = './pricing/call/price';
        localStorageKey = 'call_promocode_id';
    } else {
        promoSpinner.classList.remove('visible');
        return;
    }

    try {
        const promoResponse = await fetch('./promocodes?text=' + encodeURIComponent(code));
        if (promoResponse.status === 404) {
            setPromoError('✗ Такого промокода не существует');
            return;
        }
        const promocode = await promoResponse.json();

        if (!promocode.active || promocode.maxUsesNumber <= promocode.counterUsed) {
            setPromoError('✗ Этот промокод недоступен. Попробуйте другой');
            return;
        }

        const compatible = await fetch(
            './servicetypes/checkpromocode?promocode_id=' + promocode.id + '&service_type=' + serviceType
        ).then(r => r.text()).then(t => JSON.parse(t));

        if (!compatible) {
            setPromoError('✗ Промокод не подходит для этой услуги');
            return;
        }

        const basePrice = await fetch(priceEndpoint).then(r => r.text());
        const discountedPrice = Math.round(Number(basePrice) * (1 - promocode.discountPercent / 100));

        if (discountedPrice === 0) {
            setPromoError('✗ Скидка даёт 0 р. — выберите другой промокод');
            return;
        }

        activePromocodeId = promocode.id;
        activePromoDiscount = promocode.discountPercent;
        localStorage.setItem(localStorageKey, promocode.id);

        document.getElementById('pay-button').innerHTML = `К ОПЛАТЕ ${discountedPrice} р.`;
        setPromoSuccess(`✓ Скидка ${promocode.discountPercent}% применена`);
    } catch (e) {
        setPromoError('✗ Не удалось проверить промокод');
    }
}

promoInput.addEventListener('input', () => {
    clearTimeout(promoDebounceTimer);
    const code = promoInput.value.trim();
    if (code.length === 0) {
        resetPromo();
        return;
    }
    promoSpinner.classList.add('visible');
    promoStatus.textContent = '';
    promoStatus.className = 'field__promo-status';
    promoDebounceTimer = setTimeout(checkPromo, 2000);
});

// ─── PDF field ─────────────────────────────────────────────────

clearInput.addEventListener('click', () => {
    try { pdfInput.value = null; } catch (ex) {}
    if (pdfInput.value) {
        pdfInput.parentNode.replaceChild(pdfInput.cloneNode(true), pdfInput);
    }
    pdfLabel.innerText = 'выберите файл';
});

pdfInput.addEventListener('change', () => {
    document.getElementById('pdf-label').innerText = pdfInput.files[0].name;
});

// ─── Navigation ────────────────────────────────────────────────

burger.addEventListener('click', () => {
    headerLinks.classList.toggle('header__links_active');
});

linksClose.addEventListener('click', () => {
    headerLinks.classList.remove('header__links_active');
});

resumeButton.addEventListener('click', () => {
    resume.scrollIntoView({ behavior: 'smooth' });
});

mentoringButton.addEventListener('click', () => {
    mentoring.scrollIntoView({ behavior: 'smooth' });
});

callButton.addEventListener('click', () => {
    call.scrollIntoView({ behavior: 'smooth' });
});

// ─── Open modal ────────────────────────────────────────────────

async function openModal(title, showPdf, ofertaHref, priceEndpoint, payLabel) {
    clearAllErrors();
    resetPromo();
    form.reset();
    pdfLabel.innerText = 'твой pdf';

    popup.classList.remove('popup_hidden');
    popupTitle.innerText = title;
    pdfField.style.display = showPdf ? 'flex' : 'none';
    pdfInput.required = showPdf;
    contractLink.href = ofertaHref;

    document.getElementById('pay-button').innerHTML = payLabel;
    const response = await fetch(priceEndpoint);
    const price = await response.text();
    document.getElementById('pay-button').innerHTML = `К ОПЛАТЕ ${price} р.`;
}

showUpgradeButton.addEventListener('click', () => {
    openModal(CV_TITLE, true, './others/resume_review_pferta.pdf', './pricing/roasting/price', 'Загрузка...');
});

showMentoringButton.addEventListener('click', () => {
    openModal(MENTORING_TITLE, false, './others/mentoring_subscription_pferta.pdf', './pricing/mentoring/price', 'Загрузка...');
});

showCallButton.addEventListener('click', () => {
    openModal(CALL_TITLE, false, './others/personal_call_pferta.pdf', './pricing/call/price', 'Загрузка...');
});

popupClose.addEventListener('click', () => {
    popup.classList.add('popup_hidden');
    form.reset();
    clearInput.click();
    resetPromo();
    clearAllErrors();
    ['cv_promocode_id', 'mentoring_promocode_id', 'call_promocode_id'].forEach(k => localStorage.removeItem(k));
});

// ─── Validation helpers ────────────────────────────────────────

function normalizePhone(raw) {
    const digits = raw.replace(/\D/g, '');
    return '7' + digits.substr(digits.length - 10);
}

function normalizeTg(raw) {
    const toRemove = ['t.me', 'https'];
    let result = toRemove.reduce((acc, s) => acc.replace(new RegExp(s, 'g'), ''), raw);
    return result.replace(/[^\w]/g, '');
}

function validatePhone(value) {
    if (!value.trim()) return 'Введите номер телефона';
    const digits = value.replace(/\D/g, '');
    if (digits.length < 10) return 'Не похоже на телефон. Проверьте номер';
    return null;
}

function validateTg(value) {
    if (!value.trim()) return 'Введите ник в Telegram';
    return null;
}

function validateName(value) {
    if (!value.trim()) return 'Введите имя';
    return null;
}

// ─── Submit ────────────────────────────────────────────────────

form.addEventListener('submit', async (event) => {
    event.preventDefault();
    clearAllErrors();

    const phoneVal = document.getElementById('phone').value;
    const nameVal = document.getElementById('name').value;
    const tgVal = document.getElementById('tg').value;

    let hasError = false;

    const phoneErr = validatePhone(phoneVal);
    if (phoneErr) { setFieldError('phone', phoneErr); hasError = true; }

    const nameErr = validateName(nameVal);
    if (nameErr) { setFieldError('name', nameErr); hasError = true; }

    const tgErr = validateTg(tgVal);
    if (tgErr) { setFieldError('tg', tgErr); hasError = true; }

    if (popupTitle.innerText === CV_TITLE) {
        const file = document.getElementById('pdf').files[0];
        if (!file) { setFieldError('pdf', 'Выберите PDF файл'); hasError = true; }
    }

    if (!document.getElementById('contract').checked) {
        showFormError('Необходимо согласие с договором оферты и обработкой персональных данных');
        hasError = true;
    } else if (!document.getElementById('personal-data').checked) {
        showFormError('Необходимо согласие с обработкой персональных данных');
        hasError = true;
    }

    if (hasError) return;

    if (popupTitle.innerText === CV_TITLE) {
        await submit_new_client_cv_roasting();
    } else if (popupTitle.innerText === MENTORING_TITLE) {
        await submit_new_client_mentoringSubscription();
    } else if (popupTitle.innerText === CALL_TITLE) {
        await submit_new_client_personalCall();
    }
});

// ─── Submit functions ──────────────────────────────────────────

async function submit_new_client_cv_roasting() {
    const cvPromocodeId = localStorage.getItem('cv_promocode_id');
    localStorage.removeItem('cv_promocode_id');

    const file = document.getElementById('pdf').files[0];
    let tgName = normalizeTg(document.getElementById('tg').value);
    const phone = normalizePhone(document.getElementById('phone').value);

    const form_data = new FormData();
    form_data.append('form_data', file);

    const headers = new Headers();
    headers.append('TG-NAME', encodeURIComponent(tgName));
    headers.append('PHONE', encodeURIComponent(phone));
    headers.append('CV-PROMOCODE-ID', encodeURIComponent(cvPromocodeId ?? null));

    await fetch('./pricing/cv', {
        method: 'POST',
        body: form_data,
        headers: headers
    })
        .then(response => {
            if (response.ok) return response.text();
            throw new Error('Что-то пошло не так :(');
        })
        .then(text => {
            popup.classList.add('popup_hidden');
            form.reset();
            window.open(text, '_blank');
        })
        .catch(error => showFormError(error.message));
}

async function submit_new_client_mentoringSubscription() {
    const mentoringPromocodeId = localStorage.getItem('mentoring_promocode_id');
    localStorage.removeItem('mentoring_promocode_id');

    let tgName = normalizeTg(document.getElementById('tg').value);
    const phone = normalizePhone(document.getElementById('phone').value);

    const mentoring_data = {
        'TG-NAME': tgName,
        'PHONE': phone,
        'MENTORING-PROMOCODE-ID': mentoringPromocodeId ?? null
    };

    await fetch('./pricing/mentoring', {
        method: 'POST',
        body: JSON.stringify(mentoring_data),
        headers: { 'Content-Type': 'application/json; charset=UTF-8' }
    })
        .then(response => {
            if (response.ok) return response.text();
            throw new Error('Что-то пошло не так :(');
        })
        .then(text => {
            popup.classList.add('popup_hidden');
            form.reset();
            window.open(text, '_blank');
        })
        .catch(error => showFormError(error.message));
}

async function submit_new_client_personalCall() {
    const callPromocodeId = localStorage.getItem('call_promocode_id');
    localStorage.removeItem('call_promocode_id');

    let tgName = normalizeTg(document.getElementById('tg').value);
    const phone = normalizePhone(document.getElementById('phone').value);

    const call_data = {
        'TG-NAME': tgName,
        'PHONE': phone,
        'CALL-PROMOCODE-ID': callPromocodeId ?? null
    };

    await fetch('./pricing/call', {
        method: 'POST',
        body: JSON.stringify(call_data),
        headers: { 'Content-Type': 'application/json; charset=UTF-8' }
    })
        .then(response => {
            if (response.ok) return response.text();
            throw new Error('Что-то пошло не так :(');
        })
        .then(text => {
            popup.classList.add('popup_hidden');
            form.reset();
            window.open(text, '_blank');
        })
        .catch(error => showFormError(error.message));
}

// ─── Price display on page load ────────────────────────────────

async function getMentoringPrice() {
    const price = await fetch('./pricing/mentoring/price').then(r => r.text());
    document.getElementById('mentoring_price').innerHTML += `${price} руб/мес`;
}

async function getRoastingPrice() {
    const price = await fetch('./pricing/roasting/price').then(r => r.text());
    document.getElementById('roasting_price').innerHTML += `${price} руб`;
}

async function getCallPrice() {
    const price = await fetch('./pricing/call/price').then(r => r.text());
    document.getElementById('call_price').innerHTML += `${price} руб`;
}

getMentoringPrice();
getRoastingPrice();
getCallPrice();
