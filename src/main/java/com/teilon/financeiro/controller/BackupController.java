package com.teilon.financeiro.controller;

import com.teilon.financeiro.service.BackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/backup")
@RequiredArgsConstructor
@Tag(name = "Backup", description = "Exportar e importar dados")
@SecurityRequirement(name = "bearerAuth")
public class BackupController {

    private final BackupService backupService;

    @GetMapping("/exportar")
    @Operation(summary = "Exportar todos os dados do usuário em JSON")
    public ResponseEntity<Map<String, Object>> exportar() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"financeiro-backup.json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(backupService.exportar());
    }

    @PostMapping("/importar")
    @Operation(summary = "Importar dados a partir de um backup JSON",
               description = "Adiciona os dados do backup ao usuário atual sem apagar os existentes.")
    public ResponseEntity<Map<String, Object>> importar(@RequestBody Map<String, Object> backup) {
        return ResponseEntity.ok(backupService.importar(backup));
    }
}
