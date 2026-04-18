package com.ejemplo.servlet;

import com.ejemplo.model.CarritoItem;
import com.ejemplo.model.Producto;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/carrito")
public class CarritoServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String accion = req.getParameter("accion");
        HttpSession session = req.getSession();

        @SuppressWarnings("unchecked")
        Map<Integer, CarritoItem> carrito =
                (Map<Integer, CarritoItem>) session.getAttribute("carrito");

        if (carrito == null) {
            carrito = new LinkedHashMap<>();
            session.setAttribute("carrito", carrito);
        }

        if ("agregar".equals(accion)) {
            int idProd = Integer.parseInt(req.getParameter("idProducto"));

            // Buscar producto en el contexto de aplicación
            Producto prod = obtenerProducto(idProd);

            if (prod != null) {
                carrito.merge(idProd, new CarritoItem(prod, 1), (existing, nuevo) -> {
                    existing.setCantidad(existing.getCantidad() + 1); // fix: operador + partido
                    return existing;
                });
            }

        } else if ("limpiar".equals(accion)) {
            carrito.clear();
        }

        String destino = "verCarrito".equals(accion) ? "/carrito" : "/catalogo";
        resp.sendRedirect(req.getContextPath() + destino);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        @SuppressWarnings("unchecked")
        Map<Integer, CarritoItem> carrito = session != null
                ? (Map<Integer, CarritoItem>) session.getAttribute("carrito")
                : null;

        req.setAttribute("items", carrito != null
                ? carrito.values()
                : Collections.emptyList());

        req.getRequestDispatcher("/WEB-INF/views/carrito.jsp")
                .forward(req, resp);
    }

    private Producto obtenerProducto(int id) {
        // Leer catálogo compartido desde el contexto de aplicación
        @SuppressWarnings("unchecked")
        List<Producto> catalogo =
                (List<Producto>) getServletContext().getAttribute("catalogo");
        if (catalogo == null) return null;
        return catalogo.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }
}