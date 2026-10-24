document.querySelectorAll('[data-modal-open]').forEach(function (btn) {
	
	/*
	 * ControlFit - JavaScript de las vistas del proyecto.
	 * Gestiona modales, formularios, validaciones de envío,
	 * cálculo de membresías, navegación y búsqueda en tablas.
	 */
	
    btn.addEventListener('click', function () {
        document.getElementById(btn.dataset.modalOpen).style.display = 'flex';
    });
});
document.querySelectorAll('.modal-overlay').forEach(function (overlay) {
    overlay.addEventListener('click', function (e) {
        if (e.target === overlay || e.target.hasAttribute('data-modal-close')) {
            overlay.style.display = 'none';
        }
    });
});

document.querySelectorAll('[data-edit-action]').forEach(function (btn) {
    btn.addEventListener('click', function () {
        var modal = document.getElementById(btn.dataset.modalOpen);
        var form = modal ? modal.querySelector('form') : null;
        if (!form) { return; }
        form.reset();
        form.setAttribute('action', btn.dataset.editAction);
        Object.keys(btn.dataset).forEach(function (clave) {
            if (clave === 'modalOpen' || clave === 'editAction') { return; }
            var campo = form.elements[clave];
            if (!campo) { return; }
            var valor = btn.dataset[clave];
            if (campo.tagName === 'SELECT' && campo.multiple) {
                var ids = valor.split(',');
                Array.prototype.forEach.call(campo.options, function (op) { op.selected = ids.indexOf(op.value) !== -1; });
            } else {
                campo.value = valor;
            }
        });
    });
});

document.querySelectorAll('form').forEach(function (form) {
    var radios = form.querySelectorAll('input[name="modoCuenta"]');
    if (!radios.length) { return; }
    var bloques = form.querySelectorAll('[data-cuenta]');
    function aplicar() {
        var modo = form.querySelector('input[name="modoCuenta"]:checked').value;
        bloques.forEach(function (bloque) {
            var activo = bloque.dataset.cuenta === modo;
            bloque.hidden = !activo;
            bloque.querySelectorAll('input, select').forEach(function (campo) { campo.disabled = !activo; });
        });
    }
    radios.forEach(function (r) { r.addEventListener('change', aplicar); });
    aplicar();
});

document.querySelectorAll('form[data-confirmar], form[data-una-vez]').forEach(function (form) {
    form.addEventListener('submit', function (e) {
        if (form.dataset.confirmar && !window.confirm(form.dataset.confirmar)) {
            e.preventDefault();
            return;
        }
        if (form.hasAttribute('data-una-vez')) {
            form.querySelectorAll('button[type="submit"]').forEach(function (b) { b.disabled = true; });
        }
    });
});

document.querySelectorAll('.modal-overlay[data-autoabrir="true"]').forEach(function (m) { m.style.display = 'flex'; });

document.querySelectorAll('form[data-venta]').forEach(function (form) {
    var selTipo = form.querySelector('[name=idTipo]');
    var selPromo = form.querySelector('[name=idPromocion]');
    var inpInicio = form.querySelector('[name=fechaInicio]');
    var resumen = form.querySelector('.resumen');
    if (!selTipo || !selPromo || !resumen) { return; }

    function actualizar() {
        var tipo = selTipo.options[selTipo.selectedIndex];
        for (var i = 0; i < selPromo.options.length; i++) {
            var op = selPromo.options[i];
            if (op.value === '') { continue; }
            var ids = (op.dataset.tipos || '').split(',');
            var ok = tipo && tipo.value !== '' && ids.indexOf(tipo.value) !== -1;
            op.disabled = !ok;
            op.hidden = !ok;
            if (!ok && op.selected) { selPromo.value = ''; }
        }
        if (!tipo || tipo.value === '') {
            resumen.textContent = 'Seleccione un tipo de membresía para ver el total a cobrar.';
            return;
        }
        var base = parseFloat(tipo.dataset.precio);
        var dias = parseInt(tipo.dataset.dias, 10);
        var promo = selPromo.options[selPromo.selectedIndex];
        var desc = 0;
        if (promo && promo.value !== '') {
            var valor = parseFloat(promo.dataset.valor);
            desc = promo.dataset.tipoDescuento === 'PORCENTAJE' ? base * valor / 100 : Math.min(valor, base);
        }
        var texto = 'Precio: S/ ' + base.toFixed(2) + ' · Descuento: S/ ' + desc.toFixed(2) +
                    ' · Total a cobrar: S/ ' + (base - desc).toFixed(2);
        if (inpInicio && inpInicio.value) {
            var d = new Date(inpInicio.value + 'T00:00:00');
            d.setDate(d.getDate() + dias - 1);
            texto += ' · Vence: ' + d.getFullYear() + '-' + ('0' + (d.getMonth() + 1)).slice(-2) + '-' + ('0' + d.getDate()).slice(-2);
        }
        resumen.textContent = texto;
    }
    selTipo.addEventListener('change', actualizar);
    selPromo.addEventListener('change', actualizar);
    if (inpInicio) { inpInicio.addEventListener('change', actualizar); }
    actualizar();
});


(function () {
    var menu = document.querySelectorAll('.menu-item[href^="#"]');
    var enlaces = document.querySelectorAll('.menu-item[href^="#"], .action-card[href^="#"]');
    var bloques = document.querySelectorAll('[data-modulo]');
    var main = document.querySelector('.main-content');
    if (!menu.length || !bloques.length) { return; }

    function mostrar(modulo) {
        var existe = false;
        menu.forEach(function (a) { if (a.getAttribute('href') === '#' + modulo) { existe = true; } });
        if (!existe) { modulo = 'inicio'; }
        bloques.forEach(function (b) {
            b.hidden = b.dataset.modulo.split(' ').indexOf(modulo) === -1;
        });
        menu.forEach(function (a) {
            a.classList.toggle('active', a.getAttribute('href') === '#' + modulo);
        });
        if (main) { main.scrollTop = 0; }
    }

    enlaces.forEach(function (a) {
        a.addEventListener('click', function (e) {
            e.preventDefault();
            var modulo = a.getAttribute('href').substring(1);
            mostrar(modulo);
            history.replaceState(null, '', '#' + modulo);
        });
    });
    mostrar(location.hash.substring(1) || 'inicio');
})();


(function () {
    var COLUMNAS_FILTRO = ['Estado', 'Rol', 'Método', 'Plan', 'Cuenta'];
    var COLUMNAS_IGNORAR = ['Acciones', 'Acción'];

    function normalizar(texto) {
        return (texto || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().replace(/\s+/g, ' ').trim();
    }

    document.querySelectorAll('table[data-buscable]').forEach(function (tabla) {
        var cuerpo = tabla.tBodies[0];
        if (!cuerpo) { return; }

        var cabeceras = Array.prototype.map.call(tabla.querySelectorAll('thead th'),
            function (th) { return th.textContent.trim(); });
        var filas = Array.prototype.filter.call(cuerpo.rows, function (tr) { return !tr.querySelector('td[colspan]'); });
        if (!filas.length) { return; }   

        var datos = filas.map(function (tr) {
            var celdas = Array.prototype.map.call(tr.cells, function (td, i) {
                return COLUMNAS_IGNORAR.indexOf(cabeceras[i]) !== -1 ? null : normalizar(td.textContent);
            });
            return { fila: tr, celdas: celdas, texto: celdas.filter(function (c) { return c !== null; }).join(' ') };
        });

        var caja = document.createElement('div');
        caja.className = 'tabla-buscador';

        var etiqueta = document.createElement('label');
        etiqueta.className = 'search-box';
        var campos = cabeceras.filter(function (h) {
            return COLUMNAS_IGNORAR.indexOf(h) === -1 && COLUMNAS_FILTRO.indexOf(h) === -1;
        }).slice(0, 3).join(', ');
        etiqueta.innerHTML = '<i class="fa-solid fa-magnifying-glass"></i>' +
            '<input type="search" autocomplete="off" aria-label="Buscar en la tabla">';
        var entrada = etiqueta.querySelector('input');
        entrada.placeholder = 'Buscar por ' + campos + '…';
        caja.appendChild(etiqueta);

        var selectores = [];
        cabeceras.forEach(function (h, i) {
            if (COLUMNAS_FILTRO.indexOf(h) === -1) { return; }
            var vistos = {};
            datos.forEach(function (d) {
                if (d.celdas[i]) { vistos[d.celdas[i]] = d.fila.cells[i].textContent.trim(); }
            });
            var claves = Object.keys(vistos).sort();
            if (claves.length < 2) { return; }   
            var sel = document.createElement('select');
            sel.className = 'filtro-select';
            sel.setAttribute('aria-label', 'Filtrar por ' + h);
            sel.innerHTML = '<option value="">' + h + ': todos</option>';
            claves.forEach(function (k) {
                var op = document.createElement('option');
                op.value = k;
                op.textContent = vistos[k];
                sel.appendChild(op);
            });
            sel.dataset.columna = i;
            selectores.push(sel);
            caja.appendChild(sel);
        });

        var envoltura = tabla.closest('.table-container') || tabla;
        var anterior = envoltura.previousElementSibling;
        var contador = null;
        var textoContador = '';
        if (anterior && anterior.classList.contains('controls-bar')) {
            contador = anterior.querySelector('.muted');
            textoContador = contador ? contador.textContent : '';
            anterior.insertBefore(caja, anterior.querySelector('.btn-primary'));
        } else {
            caja.classList.add('tabla-filtros-solo');
            envoltura.parentNode.insertBefore(caja, envoltura);
        }

        var vacia = document.createElement('tr');
        vacia.hidden = true;
        vacia.innerHTML = '<td class="center muted" colspan="' + cabeceras.length + '">No se encontraron registros con ese criterio.</td>';
        cuerpo.appendChild(vacia);

        function filtrar() {
            var terminos = normalizar(entrada.value).split(' ').filter(Boolean);
            var visibles = 0;
            datos.forEach(function (d) {
                var ok = terminos.every(function (t) { return d.texto.indexOf(t) !== -1; });
                for (var s = 0; ok && s < selectores.length; s++) {
                    var valor = selectores[s].value;
                    if (valor && d.celdas[selectores[s].dataset.columna] !== valor) { ok = false; }
                }
                d.fila.hidden = !ok;
                if (ok) { visibles++; }
            });
            vacia.hidden = visibles > 0;
            if (contador) {
                var activo = terminos.length > 0 || selectores.some(function (s) { return s.value; });
                var m = textoContador.match(/^(\d+)\s+(.*)$/);
                contador.textContent = (activo && m) ? visibles + ' de ' + m[1] + ' ' + m[2] : textoContador;
            }
        }

        entrada.addEventListener('input', filtrar);
        entrada.addEventListener('keydown', function (e) {
            if (e.key === 'Escape') { entrada.value = ''; filtrar(); }
        });
        selectores.forEach(function (s) { s.addEventListener('change', filtrar); });
    });
})();
