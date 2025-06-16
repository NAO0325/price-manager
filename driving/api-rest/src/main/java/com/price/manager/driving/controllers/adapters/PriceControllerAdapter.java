package com.price.manager.driving.controllers.adapters;

import java.time.OffsetDateTime;

import com.price.manager.application.ports.driving.PriceServicePort;
import com.price.manager.driving.controllers.api.PriceControllerApi;
import com.price.manager.driving.controllers.error.PriceNotFoundException;
import com.price.manager.driving.controllers.mappers.PriceMapper;
import com.price.manager.driving.controllers.models.PriceResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador REST que expone las funcionalidades de gestión de precios a través de HTTP.
 *
 * <p>Esta clase implementa el patrón <strong>Adapter</strong> en la arquitectura hexagonal,
 * actuando como punto de entrada (driving adapter) que traduce las peticiones HTTP
 * en llamadas al dominio de la aplicación.</p>
 *
 * <p><strong>Responsabilidades:</strong></p>
 * <ul>
 *   <li><strong>Traducción de protocolos:</strong> HTTP ↔ Dominio</li>
 *   <li><strong>Validación de entrada:</strong> Parámetros HTTP válidos</li>
 *   <li><strong>Mapeo de datos:</strong> DTOs ↔ Entidades de dominio</li>
 *   <li><strong>Manejo de errores:</strong> Conversión a respuestas HTTP apropiadas</li>
 * </ul>
 *
 * <p><strong>API Contract:</strong></p>
 * <p>Implementa la especificación OpenAPI 3.0 definida en {@code openapi.yaml},
 * garantizando consistencia entre documentación y código.</p>
 *
 * <p><strong>Endpoints Expuestos:</strong></p>
 * <ul>
 *   <li><strong>GET</strong> {@code /v1/price/findByBrandProductBetweenDate}</li>
 * </ul>
 *
 * <p><strong>Ejemplo de Petición:</strong></p>
 * <pre>
 * GET /v1/price/findByBrandProductBetweenDate?dateQuery=2020-06-14T16:00:00Z&amp;productId=35455&amp;brandId=1
 *
 * Response:
 * {
 *   "id": 2,
 *   "brandId": 1,
 *   "price": 25.45,
 *   "startDate": "2020-06-14T15:00:00Z",
 *   "endDate": "2020-06-14T18:30:00Z"
 * }
 * </pre>
 *
 * @version 1.0.0
 * @since 1.0.0
 *
 * @see PriceControllerApi
 * @see PriceServicePort
 * @see PriceMapper
 * @see PriceResponse
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class PriceControllerAdapter implements PriceControllerApi {

    /**
     * Puerto de entrada al dominio para operaciones de precio.
     *
     * <p>Inyectado por Spring IoC, proporciona desacoplamiento de la implementación
     * específica del caso de uso.</p>
     */
    private final PriceServicePort priceServicePort;

    /**
     * Mapper para conversión entre entidades de dominio y DTOs de respuesta.
     *
     * <p>Implementado con MapStruct para mapeo automático y type-safe.</p>
     */
    private final PriceMapper mapper;

    /**
     * Busca el precio aplicable para una marca y producto en una fecha específica.
     *
     * <p>Este endpoint implementa el requerimiento principal de la API de precios:
     * dada una marca, producto y fecha, retorna el precio que debe aplicarse según las
     * reglas de prioridad y vigencia temporal.</p>
     *
     * <p><strong>Lógica de Negocio Aplicada:</strong></p>
     * <ol>
     *   <li><strong>Filtrado temporal:</strong> Solo precios vigentes en la fecha</li>
     *   <li><strong>Prioridad:</strong> Mayor prioridad prevalece</li>
     *   <li><strong>Desempate:</strong> Mayor priceList gana en caso de empate</li>
     * </ol>
     *
     * <p><strong>Conversiones Realizadas:</strong></p>
     * <ul>
     *   <li>{@code OffsetDateTime} → {@code LocalDateTime} para el dominio</li>
     *   <li>{@code Price} (dominio) → {@code PriceResponse} (DTO)</li>
     *   <li>{@code LocalDateTime} → {@code OffsetDateTime} en UTC para la respuesta</li>
     * </ul>
     *
     * <p><strong>Casos de Uso Cubiertos:</strong></p>
     * <ul>
     *   <li><strong>Precio encontrado (200):</strong> Retorna precio aplicable</li>
     *   <li><strong>No encontrado (404):</strong> No hay precio vigente</li>
     *   <li><strong>Parámetros inválidos (400):</strong> Validación de entrada fallida</li>
     * </ul>
     *
     * @param brandId   identificador de la marca (ej: 1 para ZARA).
     *                  Debe ser un número entero positivo.
     * @param productId identificador del producto (ej: 35455).
     *                  Debe ser un número entero positivo.
     * @param dateQuery fecha y hora para buscar precios aplicables.
     *                  Formato ISO 8601 con timezone (ej: 2020-06-14T16:00:00Z).
     *
     * @return {@link ResponseEntity} con:
     *         <ul>
     *           <li><strong>200 OK:</strong> {@link PriceResponse} con datos del precio</li>
     *           <li><strong>404 NOT_FOUND:</strong> No se encontró precio aplicable</li>
     *           <li><strong>400 BAD_REQUEST:</strong> Parámetros inválidos</li>
     *         </ul>
     *
     * @throws PriceNotFoundException   si no se encuentra precio aplicable para los criterios
     * @throws IllegalArgumentException si los parámetros de entrada son inválidos
     *
     * @see #priceServicePort
     * @see #mapper
     * @see PriceResponse
     * @see com.price.manager.domain.Price
     *
     * @since 1.0.0
     */
    @Override
    public ResponseEntity<PriceResponse> findByBrandProductBetweenDate(Long brandId, Long productId,
                                                                       OffsetDateTime dateQuery) {

        final var price = this.priceServicePort.findByBrandProductBetweenDate(
                brandId,
                productId,
                dateQuery.toLocalDateTime()
        );

        final var response = this.mapper.toResponseDto(price);

        if (response == null) {
            throw new PriceNotFoundException("No price found for the given parameters");
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
