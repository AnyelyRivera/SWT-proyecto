package org.example;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * Clase principal para gestionar la información de estudiantes.
 * Utiliza SWT para crear una interfaz gráfica con dos pantallas:
 * - Pantalla principal para ingresar datos y calcular promedios.
 * - Pantalla de detalles para ver historial y agregar asignaturas.
 */
@SuppressWarnings("SpellCheckingInspection")
public class GestorEstudiantes {
    private static final int MAX_NOMBRE_LENGTH = 50; // Límite máximo de caracteres para el nombre
    private static final int MAX_NOTA = 100;        // Nota máxima permitida
    private static final int MIN_NOTA = 0;          // Nota mínima permitida
    private static final double NOTA_APROBACION = 70.0; // Umbral para aprobar

    // Shells para las dos pantallas
    private final Shell shellPrincipal;
    private Shell shellDetalles;

    // Componentes pantalla principal
    private Text txtNombre;
    private final Text[] txtParciales = new Text[3];
    private Label lblResultado;
    private Button btnModoNoche;
    private boolean modoNoche = false;
    private Color colorTextoNormal;
    private Color colorFondoNormal;
    private Color colorTextoNoche;
    private Color colorFondoNoche;

    // Componentes pantalla de detalles
    private Table tblHistorial;
    private Text txtComentarios;
    private Combo cmbCalificacion;

    // Componentes para agregar asignaturas
    private Text txtAsignatura;
    private Text txtNota;
    private Combo cmbSemestre;

    // Control para mantener datos entre pantallas
    private String nombreEstudiante;
    private double promedioCalculado;
    private String estadoFinal;

    /**
     * Constructor que inicializa la aplicación con un Display de SWT.
     * @param display El Display de SWT para renderizar la interfaz.
     */
    public GestorEstudiantes(Display display) {
        shellPrincipal = new Shell(display);
        inicializarColores();
        inicializarComponentesPrincipal();
        configurarEventosPrincipal();
        configurarValidaciones();
    }

    /**
     * Inicializa los colores predeterminados para los modos día y noche.
     */
    private void inicializarColores() {
        try {
            colorTextoNormal = shellPrincipal.getDisplay().getSystemColor(SWT.COLOR_BLACK);
            colorFondoNormal = shellPrincipal.getDisplay().getSystemColor(SWT.COLOR_WHITE);
            colorTextoNoche = shellPrincipal.getDisplay().getSystemColor(SWT.COLOR_WHITE);
            colorFondoNoche = shellPrincipal.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
        } catch (Exception e) {
            mostrarError("Error al inicializar colores: " + e.getMessage());
        }
    }

    /**
     * Inicializa los componentes de la pantalla principal.
     */
    private void inicializarComponentesPrincipal() {
        try {
            shellPrincipal.setSize(400, 350);
            shellPrincipal.setText("Gestión Estudiantil - Pantalla Principal");
            shellPrincipal.setLayout(new GridLayout(2, false));

            // Componentes para el nombre
            new Label(shellPrincipal, SWT.NONE).setText("Nombre del estudiante:");
            txtNombre = new Text(shellPrincipal, SWT.BORDER);
            txtNombre.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            txtNombre.setTextLimit(MAX_NOMBRE_LENGTH);

            // Componentes para las notas
            for (int i = 0; i < 3; i++) {
                new Label(shellPrincipal, SWT.NONE).setText("Parcial " + (i + 1) + ":");
                txtParciales[i] = new Text(shellPrincipal, SWT.BORDER);
                txtParciales[i].setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            }

            // Botón calcular
            Button btnCalcular = new Button(shellPrincipal, SWT.PUSH);
            btnCalcular.setText("Calcular Promedio");
            btnCalcular.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
            btnCalcular.addListener(SWT.Selection, e -> calcularPromedio());

            // Botón modo noche
            btnModoNoche = new Button(shellPrincipal, SWT.PUSH);
            btnModoNoche.setText("Modo Noche");
            btnModoNoche.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

            // Label resultado
            lblResultado = new Label(shellPrincipal, SWT.CENTER);
            lblResultado.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

            // Botón para ir a la pantalla de detalles
            Button btnVerDetalles = new Button(shellPrincipal, SWT.PUSH);
            btnVerDetalles.setText("Ver Detalles");
            btnVerDetalles.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
            btnVerDetalles.addListener(SWT.Selection, e -> abrirPantallaDetalles());

        } catch (Exception e) {
            mostrarError("Error al inicializar componentes: " + e.getMessage());
        }
    }

    /**
     * Inicializa los componentes de la pantalla de detalles, incluyendo el botón "Salir".
     */
    private void inicializarComponentesDetalles() {
        try {
            shellDetalles = new Shell(shellPrincipal.getDisplay(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
            shellDetalles.setSize(500, 660);
            shellDetalles.setText("Detalles del Estudiante");
            shellDetalles.setLayout(new GridLayout(2, false));

            // Convertir fields a variables locales
            Label lblNombreDetalle;
            Label lblPromedioDetalle;
            Label lblEstadoDetalle;
            Button btnGuardar;

            // Grupo de información del estudiante
            Group grpInfo = new Group(shellDetalles, SWT.NONE);
            grpInfo.setText("Información del Estudiante");
            grpInfo.setLayout(new GridLayout(2, false));
            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
            grpInfo.setLayoutData(gridData);

            new Label(grpInfo, SWT.NONE).setText("Nombre:");
            lblNombreDetalle = new Label(grpInfo, SWT.NONE);
            lblNombreDetalle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            lblNombreDetalle.setText(nombreEstudiante);

            new Label(grpInfo, SWT.NONE).setText("Promedio:");
            lblPromedioDetalle = new Label(grpInfo, SWT.NONE);
            lblPromedioDetalle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            lblPromedioDetalle.setText(String.format("%.2f", promedioCalculado));

            new Label(grpInfo, SWT.NONE).setText("Estado:");
            lblEstadoDetalle = new Label(grpInfo, SWT.NONE);
            lblEstadoDetalle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            lblEstadoDetalle.setText(estadoFinal);

            // Grupo para agregar asignatura
            Group grpAgregarAsignatura = new Group(shellDetalles, SWT.NONE);
            grpAgregarAsignatura.setText("Agregar Asignatura");
            grpAgregarAsignatura.setLayout(new GridLayout(2, false));
            GridData gdAgregarAsignatura = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
            grpAgregarAsignatura.setLayoutData(gdAgregarAsignatura);

            // Campos para agregar asignatura
            new Label(grpAgregarAsignatura, SWT.NONE).setText("Asignatura:");
            txtAsignatura = new Text(grpAgregarAsignatura, SWT.BORDER);
            txtAsignatura.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

            new Label(grpAgregarAsignatura, SWT.NONE).setText("Nota:");
            txtNota = new Text(grpAgregarAsignatura, SWT.BORDER);
            txtNota.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            txtNota.addVerifyListener(e -> validarNumeroDecimal(e, txtNota));

            new Label(grpAgregarAsignatura, SWT.NONE).setText("Semestre:");
            cmbSemestre = new Combo(grpAgregarAsignatura, SWT.READ_ONLY);
            cmbSemestre.setItems("2023-1", "2023-2", "2024-1", "2024-2");
            cmbSemestre.select(0);
            cmbSemestre.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

            // Botón para agregar asignatura
            Button btnAgregarAsignatura = new Button(grpAgregarAsignatura, SWT.PUSH);
            btnAgregarAsignatura.setText("Agregar Asignatura");
            btnAgregarAsignatura.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
            btnAgregarAsignatura.addListener(SWT.Selection, e -> agregarAsignatura());

            // Tabla de historial académico
            Label lblHistorial = new Label(shellDetalles, SWT.NONE);
            lblHistorial.setText("Historial de Asignaturas:");
            lblHistorial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

            tblHistorial = new Table(shellDetalles, SWT.BORDER | SWT.FULL_SELECTION);
            tblHistorial.setLinesVisible(true);
            tblHistorial.setHeaderVisible(true);
            GridData gdTable = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
            gdTable.heightHint = 100;
            gdTable.minimumHeight = 100;
            tblHistorial.setLayoutData(gdTable);

            // Crear columnas
            String[] titles = {"Asignatura", "Nota", "Semestre", "Estado"};
            for (String title : titles) {
                TableColumn column = new TableColumn(tblHistorial, SWT.NONE);
                column.setText(title);
                column.setWidth(100);
            }

            // Añadir datos iniciales a la tabla
            agregarDatosEjemploTabla();

            // Sección de comentarios y calificación
            Group grpComentarios = new Group(shellDetalles, SWT.NONE);
            grpComentarios.setText("Evaluación del estudiante");
            grpComentarios.setLayout(new GridLayout(2, false));
            grpComentarios.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

            new Label(grpComentarios, SWT.NONE).setText("Calificación:");
            cmbCalificacion = new Combo(grpComentarios, SWT.READ_ONLY);
            cmbCalificacion.setItems("Excelente", "Bueno", "Regular", "Necesita mejorar");
            cmbCalificacion.select(0);
            cmbCalificacion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

            new Label(grpComentarios, SWT.NONE).setText("Comentarios:");
            txtComentarios = new Text(grpComentarios, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
            GridData gdComentarios = new GridData(SWT.FILL, SWT.FILL, true, false);
            gdComentarios.heightHint = 60;
            txtComentarios.setLayoutData(gdComentarios);

            // Botones de acción
            Composite btnComposite = new Composite(shellDetalles, SWT.NONE);
            btnComposite.setLayout(new GridLayout(3, false)); // Cambiado a 3 columnas para el nuevo botón
            btnComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

            btnGuardar = new Button(btnComposite, SWT.PUSH);
            btnGuardar.setText("Guardar");
            btnGuardar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
            btnGuardar.addListener(SWT.Selection, e -> guardarComentarios());

            Button btnVolver = new Button(btnComposite, SWT.PUSH);
            btnVolver.setText("Volver");
            btnVolver.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
            btnVolver.addListener(SWT.Selection, e -> shellDetalles.close());

            // Nuevo botón "Salir" para cerrar toda la aplicación
            Button btnSalir = new Button(btnComposite, SWT.PUSH);
            btnSalir.setText("Salir");
            btnSalir.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
            btnSalir.addListener(SWT.Selection, e -> {
                MessageBox mb = new MessageBox(shellDetalles, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
                mb.setMessage("¿Desea salir de la aplicación?");
                if (mb.open() == SWT.YES) {
                    shellDetalles.getDisplay().dispose(); // Cierra toda la aplicación
                }
            });

            shellDetalles.layout(true, true);

            if (modoNoche) {
                aplicarModoNochePantallaDetalles();
            }

        } catch (Exception e) {
            mostrarError("Error al inicializar pantalla de detalles: " + e.getMessage());
        }
    }

    // Método extraído para validar entradas numéricas con decimales
    private void validarNumeroDecimal(VerifyEvent e, Text campo) {
        String text = e.text;
        if (text.isEmpty()) return;
        if (text.equals(".") && !campo.getText().contains(".")) return;
        if (!text.matches("[0-9.]")) e.doit = false;
    }

    private void agregarAsignatura() {
        try {
            String asignatura = txtAsignatura.getText().trim();
            String notaStr = txtNota.getText().trim();
            String semestre = cmbSemestre.getText();

            if (asignatura.isEmpty()) {
                mostrarMensaje(shellDetalles, SWT.ICON_WARNING, "Debe ingresar el nombre de la asignatura");
                return;
            }

            if (notaStr.isEmpty()) {
                mostrarMensaje(shellDetalles, SWT.ICON_WARNING, "Debe ingresar la nota");
                return;
            }

            double nota = Double.parseDouble(notaStr);
            if (nota < MIN_NOTA || nota > MAX_NOTA) {
                mostrarMensaje(shellDetalles, SWT.ICON_WARNING,
                        "La nota debe estar entre " + MIN_NOTA + " y " + MAX_NOTA);
                return;
            }

            String estado = nota >= NOTA_APROBACION ? "Aprobado" : "Reprobado";

            TableItem item = new TableItem(tblHistorial, SWT.NONE);
            item.setText(new String[]{
                    asignatura,
                    String.format("%.1f", nota),
                    semestre,
                    estado
            });

            txtAsignatura.setText("");
            txtNota.setText("");

            mostrarMensaje(shellDetalles, SWT.ICON_INFORMATION, "Asignatura agregada correctamente");
        } catch (Exception e) {
            mostrarMensaje(shellDetalles, SWT.ICON_ERROR, "Error al agregar asignatura: " + e.getMessage());
        }
    }

    private void agregarDatosEjemploTabla() {
        String[][] datos = {
                {"Programación I", "85", "2023-1", "Aprobado"},
                {"Matemáticas", "78", "2023-1", "Aprobado"},
                {"Física", "65", "2023-1", "Reprobado"}
        };

        for (String[] fila : datos) {
            TableItem item = new TableItem(tblHistorial, SWT.NONE);
            item.setText(fila);
        }
    }

    private void configurarEventosPrincipal() {
        btnModoNoche.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                modoNoche = !modoNoche;
                actualizarModoNoche();
            }
        });

        shellPrincipal.addListener(SWT.Close, event -> {
            MessageBox mb = new MessageBox(shellPrincipal, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
            mb.setMessage("¿Desea cerrar la aplicación?");
            event.doit = mb.open() == SWT.YES;
        });
    }

    private void configurarValidaciones() {
        for (Text txtParcial : txtParciales) {
            txtParcial.addVerifyListener(e -> validarNumeroDecimal(e, txtParcial));
        }
    }

    private void calcularPromedio() {
        try {
            nombreEstudiante = txtNombre.getText().trim();
            if (nombreEstudiante.isEmpty()) {
                mostrarError("El nombre del estudiante es obligatorio");
                return;
            }

            double suma = 0;
            for (int i = 0; i < 3; i++) {
                String notaTexto = txtParciales[i].getText().trim();
                if (notaTexto.isEmpty()) {
                    mostrarError("La nota del parcial " + (i + 1) + " es obligatoria");
                    return;
                }

                double nota = Double.parseDouble(notaTexto);
                if (nota < MIN_NOTA || nota > MAX_NOTA) {
                    mostrarError("Las notas deben estar entre " + MIN_NOTA + " y " + MAX_NOTA);
                    return;
                }
                suma += nota;
            }

            promedioCalculado = suma / 3;
            estadoFinal = promedioCalculado >= NOTA_APROBACION ? "APROBADO" : "REPROBADO";
            lblResultado.setText(String.format("%s - Promedio: %.2f - %s", nombreEstudiante, promedioCalculado, estadoFinal));

        } catch (NumberFormatException ex) {
            mostrarError("Ingrese valores numéricos válidos");
        } catch (Exception ex) {
            mostrarError("Error al calcular el promedio: " + ex.getMessage());
        }
    }

    private void actualizarModoNoche() {
        try {
            Color textoColor = modoNoche ? colorTextoNoche : colorTextoNormal;
            Color fondoColor = modoNoche ? colorFondoNoche : colorFondoNormal;

            shellPrincipal.setBackground(fondoColor);
            txtNombre.setBackground(fondoColor);
            txtNombre.setForeground(textoColor);

            for (Text txtParcial : txtParciales) {
                txtParcial.setBackground(fondoColor);
                txtParcial.setForeground(textoColor);
            }

            lblResultado.setBackground(fondoColor);
            lblResultado.setForeground(textoColor);

            btnModoNoche.setText(modoNoche ? "Modo Día" : "Modo Noche");

            if (shellDetalles != null && !shellDetalles.isDisposed()) {
                aplicarModoNochePantallaDetalles();
            }

        } catch (Exception e) {
            mostrarError("Error al cambiar el modo de visualización");
        }
    }

    private void aplicarModoNochePantallaDetalles() {
        Color textoColor = modoNoche ? colorTextoNoche : colorTextoNormal;
        Color fondoColor = modoNoche ? colorFondoNoche : colorFondoNormal;

        shellDetalles.setBackground(fondoColor);

        Control[] controles = shellDetalles.getChildren();
        for (Control control : controles) {
            if (!(control instanceof Table)) {
                aplicarModoNocheRecursivo(control, textoColor, fondoColor);
            }
        }
    }

    private void aplicarModoNocheRecursivo(Control control, Color textoColor, Color fondoColor) {
        control.setBackground(fondoColor);
        control.setForeground(textoColor);

        if (control instanceof Composite) {
            for (Control hijo : ((Composite) control).getChildren()) {
                if (!(hijo instanceof Table)) {
                    aplicarModoNocheRecursivo(hijo, textoColor, fondoColor);
                }
            }
        }
    }

    private void mostrarMensaje(Shell shell, int style, String mensaje) {
        MessageBox mb = new MessageBox(shell, style | SWT.OK);
        mb.setMessage(mensaje);
        mb.open();
    }

    private void mostrarError(String mensaje) {
        mostrarMensaje(shellPrincipal, SWT.ICON_ERROR, mensaje);
    }

    private void abrirPantallaDetalles() {
        if (lblResultado.getText().isEmpty()) {
            mostrarError("Primero debe calcular el promedio del estudiante");
            return;
        }

        inicializarComponentesDetalles();
        shellDetalles.open();

        Display display = shellPrincipal.getDisplay();
        while (!shellDetalles.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    private void guardarComentarios() {
        String calificacion = cmbCalificacion.getText();
        String comentarios = txtComentarios.getText();

        if (comentarios.trim().isEmpty()) {
            MessageBox mb = new MessageBox(shellDetalles, SWT.ICON_WARNING | SWT.OK);
            mb.setMessage("Por favor ingrese algún comentario antes de guardar");
            mb.open();
            return;
        }

        MessageBox mb = new MessageBox(shellDetalles, SWT.ICON_INFORMATION | SWT.OK);
        mb.setMessage("Evaluación guardada correctamente:\nCalificación: " + calificacion +
                "\nComentarios: " + comentarios);
        mb.open();
    }

    public void open() {
        shellPrincipal.open();
    }

    public boolean isDisposed() {
        return shellPrincipal.isDisposed();
    }

    public void dispose() {
        try {
            if (colorTextoNormal != null) colorTextoNormal.dispose();
            if (colorFondoNormal != null) colorFondoNormal.dispose();
            if (colorTextoNoche != null) colorTextoNoche.dispose();
            if (colorFondoNoche != null) colorFondoNoche.dispose();
        } catch (Exception e) {
            System.err.println("Error al liberar recursos: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Display display = null;
        GestorEstudiantes gestorEstudiantes = null;

        try {
            display = new Display();
            gestorEstudiantes = new GestorEstudiantes(display);
            gestorEstudiantes.open();

            while (!gestorEstudiantes.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        } catch (Exception e) {
            MessageBox mb = new MessageBox(new Shell(display), SWT.ICON_ERROR | SWT.OK);
            mb.setMessage("Error inesperado: " + e.getMessage());
            mb.open();
        } finally {
            if (gestorEstudiantes != null) gestorEstudiantes.dispose();
            if (display != null && !display.isDisposed()) display.dispose();
        }
    }
}