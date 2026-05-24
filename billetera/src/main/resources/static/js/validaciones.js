document.addEventListener('DOMContentLoaded', function () {

    // Validar que monto sea positivo y no exceda límite
    document.querySelectorAll('input[name="monto"]').forEach(function (input) {
        input.addEventListener('input', function () {
            const val = parseFloat(this.value);
            if (val <= 0 || isNaN(val)) {
                this.setCustomValidity('El monto debe ser mayor a 0');
            } else if (val > 100000000) {
                this.setCustomValidity('El monto no puede superar $100.000.000');
            } else {
                this.setCustomValidity('');
            }
        });
    });

    // Validar que origen y destino no sean iguales
    const origenSelect = document.querySelector('select[name="origenId"]');
    const destinoSelect = document.querySelector('select[name="destinoId"]');

    if (origenSelect && destinoSelect) {
        function validarSelects() {
            if (origenSelect.value && destinoSelect.value &&
                origenSelect.value === destinoSelect.value) {
                destinoSelect.setCustomValidity('La billetera destino debe ser diferente a la de origen');
            } else {
                destinoSelect.setCustomValidity('');
            }
        }
        origenSelect.addEventListener('change', validarSelects);
        destinoSelect.addEventListener('change', validarSelects);
    }

    // Validar fecha futura
    const fechaInput = document.querySelector('input[name="fechaEjecucion"]');
    if (fechaInput) {
        function validarFecha() {
            const fecha = new Date(fechaInput.value);
            const ahora = new Date();
            if (!fechaInput.value || fecha <= ahora) {
                fechaInput.setCustomValidity('La fecha debe ser futura');
            } else {
                fechaInput.setCustomValidity('');
            }
        }
        fechaInput.addEventListener('change', validarFecha);
    }

    // Validar ID sin espacios
    document.querySelectorAll('input[name="id"]').forEach(function (input) {
        input.addEventListener('input', function () {
            if (this.value.includes(' ')) {
                this.setCustomValidity('El ID no puede contener espacios');
            } else {
                this.setCustomValidity('');
            }
        });
    });

    // Validar todos los formularios antes de enviar
    document.querySelectorAll('form').forEach(function (form) {
        form.addEventListener('submit', function (e) {

            // Validar montos
            form.querySelectorAll('input[name="monto"]').forEach(function (input) {
                const val = parseFloat(input.value);
                if (val <= 0 || isNaN(val)) {
                    input.setCustomValidity('El monto debe ser mayor a 0');
                } else if (val > 100000000) {
                    input.setCustomValidity('El monto no puede superar $100.000.000');
                } else {
                    input.setCustomValidity('');
                }
            });

            // Validar fecha futura
            const fecha = form.querySelector('input[name="fechaEjecucion"]');
            if (fecha) {
                const fechaVal = new Date(fecha.value);
                const ahora = new Date();
                if (!fecha.value || fechaVal <= ahora) {
                    fecha.setCustomValidity('La fecha debe ser futura');
                } else {
                    fecha.setCustomValidity('');
                }
            }

            // Validar ID sin espacios
            form.querySelectorAll('input[name="id"]').forEach(function (input) {
                if (input.value.includes(' ')) {
                    input.setCustomValidity('El ID no puede contener espacios');
                } else {
                    input.setCustomValidity('');
                }
            });

            // Validar origen != destino
            const origen = form.querySelector('select[name="origenId"]');
            const destino = form.querySelector('select[name="destinoId"]');
            if (origen && destino && origen.value && destino.value &&
                origen.value === destino.value) {
                destino.setCustomValidity('La billetera destino debe ser diferente a la de origen');
            } else if (destino) {
                destino.setCustomValidity('');
            }

            // Si hay errores bloquear envío
            if (!form.checkValidity()) {
                e.preventDefault();
                form.reportValidity();
            }
        });
    });
});