package com.GoPedidos.GoPedidos.Services;

import com.GoPedidos.GoPedidos.Exceptions.CustomException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;

@Service
public class CloudflareR2Service {

	private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 5MB
	private static final String IMAGE_PREFIX = "image/";
	@Value("${cloudflare.r2.bucket-url}")
	private String bucketUrl;
	@Value("${cloudflare.r2.access-key-id}")
	private String accessKeyId;
	@Value("${cloudflare.r2.secret-access-key}")
	private String secretAccessKey;

	@Value("${cloudflare.r2.public-url}")
	private String publicUrl;

	@Value("${cloudflare.r2.bucket.produtos}")
	private String bucketProdutos;

	@Getter
	@Value("${cloudflare.r2.bucket.categorias}")
	private String bucketCategorias;


	public String uploadFile(MultipartFile file, String fileName, String oldFileName, String bucketName) throws IOException {
		if (fileName == null || fileName.isEmpty()) {
			throw new CustomException("Nome do arquivo não fornecido.", HttpStatus.BAD_REQUEST.value());
		}

		S3Client s3Client = S3Client.builder()
				.region(Region.of("auto"))
				.credentialsProvider(StaticCredentialsProvider.create(
						AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
				.endpointOverride(URI.create(bucketUrl)) // Usa o endpoint privado para upload
				.build();

		// Exclui a imagem antiga, se existir
		if (oldFileName != null && !oldFileName.isEmpty()) {
			try {
				this.deleteFile(oldFileName, bucketName);
			} catch (Exception e) {
				System.err.println("Erro ao excluir a imagem antiga: " + e.getMessage());
			}
		}

		// Faz o upload do novo arquivo
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(fileName)
				.contentType(file.getContentType()) // Define o tipo de conteúdo
				.build();

		s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

		// Retorna a URL pública do arquivo
		return publicUrl + "/" + fileName; // Usa o endpoint público para a URL
	}


	public void deleteFile(String fileName, String bucketName) {
		S3Client s3Client = S3Client.builder()
				.region(Region.of("auto"))
				.credentialsProvider(StaticCredentialsProvider.create(
						AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
				.endpointOverride(URI.create(bucketUrl)) // Usa o endpoint privado
				.build();

		// Cria a requisição para excluir o arquivo
		DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
				.bucket(bucketName)
				.key(fileName)
				.build();

		// Exclui o arquivo
		s3Client.deleteObject(deleteObjectRequest);
	}


	@Bean
	public S3Client s3Client() {
		return S3Client.builder()
				.region(Region.of("auto")) // Cloudflare R2 requer "auto" como região
				.credentialsProvider(StaticCredentialsProvider.create(
						AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
				.endpointOverride(URI.create(bucketUrl)) // URL do Cloudflare R2
				.build();
	}

	// Serviço para fazer upload de imagens (produto, categoria, etc.)
	public String uploadImagem(Long entityId, MultipartFile file, String entityType, String bucketName) {
		try {
			// Valida o tipo de arquivo
			if (file.isEmpty()) {
				throw new CustomException("Arquivo de imagem não encontrado.", HttpStatus.BAD_REQUEST.value());
			}

			// Verifica se o arquivo é uma imagem
			String contentType = file.getContentType();
			if (contentType == null || !contentType.startsWith(IMAGE_PREFIX)) {
				throw new CustomException("O arquivo enviado não é uma imagem válida.", HttpStatus.BAD_REQUEST.value());
			}

			// Verifica o tamanho do arquivo (máximo 5MB)
			if (file.getSize() > MAX_FILE_SIZE) {
				throw new CustomException("O arquivo de imagem é muito grande. O tamanho máximo permitido é 5MB.", HttpStatus.BAD_REQUEST.value());
			}

			// Gera um nome único para o arquivo
			String fileName = entityType + "_" + entityId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

			// Faz o upload da imagem para o Cloudflare R2
			return uploadFile(file, fileName, null, bucketName);

		} catch (IOException e) {
			throw new CustomException("Erro ao processar o arquivo: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
		} catch (Exception e) {
			throw new CustomException("Erro inesperado ao fazer upload da imagem: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}


}