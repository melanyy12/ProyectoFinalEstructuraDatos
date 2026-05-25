document.addEventListener("DOMContentLoaded", function () {
    const toasts = document.querySelectorAll(".toast-center");

    toasts.forEach(function (toast) {
        setTimeout(function () {
            toast.classList.add("hide");

            setTimeout(function () {
                toast.remove();
            }, 300);
        }, 3000);
    });
});