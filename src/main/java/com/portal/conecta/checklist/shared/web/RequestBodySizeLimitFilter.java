package com.portal.conecta.checklist.shared.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.shared.exception.ErrorResponseDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RequestBodySizeLimitFilter extends OncePerRequestFilter {

    private static final int BUFFER_SIZE = 8 * 1024;
    private static final Set<String> METHODS_WITH_BODY = Set.of("POST", "PUT", "PATCH");

    private final RequestBodyLimitProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!METHODS_WITH_BODY.contains(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        long maxBodySizeBytes = properties.maxBodySizeBytes();
        long contentLength = request.getContentLengthLong();

        if (contentLength > maxBodySizeBytes) {
            writePayloadTooLarge(response, maxBodySizeBytes);
            return;
        }

        if (contentLength == 0) {
            filterChain.doFilter(request, response);
            return;
        }

        byte[] body = readBodyWithinLimit(request, maxBodySizeBytes);
        if (body.length > maxBodySizeBytes) {
            writePayloadTooLarge(response, maxBodySizeBytes);
            return;
        }

        filterChain.doFilter(new CachedBodyHttpServletRequest(request, body), response);
    }

    private byte[] readBodyWithinLimit(HttpServletRequest request, long maxBodySizeBytes) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        long totalBytes = 0;
        int bytesRead;
        ServletInputStream inputStream = request.getInputStream();

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            totalBytes += bytesRead;
            if (totalBytes > maxBodySizeBytes) {
                output.write(buffer, 0, (int) (bytesRead - (totalBytes - maxBodySizeBytes) + 1));
                break;
            }
            output.write(buffer, 0, bytesRead);
        }

        return output.toByteArray();
    }

    private void writePayloadTooLarge(HttpServletResponse response, long maxBodySizeBytes) throws IOException {
        response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                "Corpo da requisicao excede o limite maximo de " + maxBodySizeBytes + " bytes.",
                null
        );

        objectMapper.writeValue(response.getWriter(), error);
    }

    private static final class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

        private final byte[] body;

        private CachedBodyHttpServletRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public ServletInputStream getInputStream() {
            return new CachedBodyServletInputStream(body);
        }

        @Override
        public BufferedReader getReader() {
            Charset charset = getCharacterEncoding() == null
                    ? StandardCharsets.UTF_8
                    : Charset.forName(getCharacterEncoding());
            return new BufferedReader(new InputStreamReader(getInputStream(), charset));
        }
    }

    private static final class CachedBodyServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream inputStream;

        private CachedBodyServletInputStream(byte[] body) {
            this.inputStream = new ByteArrayInputStream(body);
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException("Async read is not supported.");
        }

        @Override
        public int read() {
            return inputStream.read();
        }
    }
}
