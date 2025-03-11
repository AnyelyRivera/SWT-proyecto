package org.example;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

@SuppressWarnings("SpellCheckingInspection")
public class GestorEstudiantes {
    private static final int MAX_NOMBRE_LENGTH = 50;
    private static final int MAX_NOTA = 100;
    private static final int MIN_NOTA = 0;
    private static final double NOTA_APROBACION = 70.0;

    private final Shell shell;
    private Text txtNombre;
    private final Text[] txtParciales = new Text[3];
    private Label lblResultado;
    private Button btnModoNoche;
    private boolean modoNoche = false;
    private Color colorTextoNormal;
    private Color colorFondoNormal;
    private Color colorTextoNoche;
    private Color colorFondoNoche;

    public GestorEstudiantes(Display display) {
        shell = new Shell(display);
        inicializarColores();
        inicializarComponentes();
        configurarEventos();
        configurarValidaciones();
    }

    private void inicializarColores() {
        try {
            colorTextoNormal = shell.getDisplay().getSystemColor(SWT.COLOR_BLACK);
            colorFondoNormal = shell.getDisplay().getSystemColor(SWT.COLOR_WHITE);
            colorTextoNoche = shell.getDisplay().getSystemColor(SWT.COLOR_WHITE);
            colorFondoNoche = shell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
        } catch (Exception e) {
            mostrarError("Error al inicializar colores: " + e.getMessage());
        }
    }

    private void inicializarComponentes() {
        try {
            shell.setSize(400, 300);
            shell.setText("Gestión Estudiantil");
            shell.setLayout(new GridLayout(2, false));

            // Componentes para el nombre
            new Label(shell, SWT.NONE).setText("Nombre del estudiante:");
            txtNombre = new Text(shell, SWT.BORDER);
            txtNombre.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            txtNombre.setTextLimit(MAX_NOMBRE_LENGTH);

            // Componentes para las notas
            for (int i = 0; i < 3; i++) {
                new Label(shell, SWT.NONE).setText("Parcial " + (i + 1) + ":");
                txtParciales[i] = new Text(shell, SWT.BORDER);
                txtParciales[i].setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            }

            // Botón calcular
            Button btnCalcular = new Button(shell, SWT.PUSH);
            btnCalcular.setText("Calcular Promedio");
            btnCalcular.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
            btnCalcular.addListener(SWT.Selection, e -> calcularPromedio());

            // Botón modo noche
            btnModoNoche = new Button(shell, SWT.PUSH);
            btnModoNoche.setText("Modo Noche");
            btnModoNoche.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

            // Label resultado
            lblResultado = new Label(shell, SWT.CENTER);
            lblResultado.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        } catch (Exception e) {
            mostrarError("Error al inicializar componentes: " + e.getMessage());
        }
    }

    private void configurarEventos() {
        btnModoNoche.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                modoNoche = !modoNoche;
                actualizarModoNoche();
            }
        });

        shell.addListener(SWT.Close, event -> {
            MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
            mb.setMessage("¿Desea cerrar la aplicación?");
            event.doit = mb.open() == SWT.YES;
        });
    }

    private void configurarValidaciones() {
        // Validación para permitir solo números en los campos de notas
        for (Text txtParcial : txtParciales) {
            txtParcial.addVerifyListener(e -> {
                String text = e.text;
                if (text.isEmpty()) return;
                if (text.equals(".") && !txtParcial.getText().contains(".")) return;
                if (!text.matches("[0-9.]")) e.doit = false;
            });
        }
    }

    private void calcularPromedio() {
        try {
            String nombre = txtNombre.getText().trim();
            if (nombre.isEmpty()) {
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

            double promedio = suma / 3;
            String estado = promedio >= NOTA_APROBACION ? "APROBADO" : "REPROBADO";
            lblResultado.setText(String.format("%s - Promedio: %.2f - %s", nombre, promedio, estado));

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

            shell.setBackground(fondoColor);
            txtNombre.setBackground(fondoColor);
            txtNombre.setForeground(textoColor);

            for (Text txtParcial : txtParciales) {
                txtParcial.setBackground(fondoColor);
                txtParcial.setForeground(textoColor);
            }

            lblResultado.setBackground(fondoColor);
            lblResultado.setForeground(textoColor);

            btnModoNoche.setText(modoNoche ? "Modo Día" : "Modo Noche");

        } catch (Exception e) {
            mostrarError("Error al cambiar el modo de visualización");
        }
    }

    private void mostrarError(String mensaje) {
        MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        mb.setMessage(mensaje);
        mb.open();
    }

    public void open() {
        shell.open();
    }

    public boolean isDisposed() {
        return shell.isDisposed();
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