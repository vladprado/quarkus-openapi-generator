package io.quarkiverse.openapi.generator.deployment;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.quarkiverse.openapi.generator.deployment.codegen.OpenApiGeneratorOutputPaths;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;
import io.smallrye.config.common.utils.StringUtil;

// This configuration is read in codegen phase (before build time), the annotation is for document purposes and avoiding quarkus warns
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
@ConfigMapping(prefix = "quarkus." + CodegenConfig.CODEGEN_TIME_CONFIG_PREFIX)
public interface CodegenConfig extends GlobalCodegenConfig {

    String CODEGEN_TIME_CONFIG_PREFIX = "openapi-generator.codegen";

    String API_PKG_SUFFIX = ".api";
    String MODEL_PKG_SUFFIX = ".model";

    String ADDITIONAL_ENUM_TYPE_UNEXPECTED_MEMBER_NAME_DEFAULT = "UNEXPECTED";
    String ADDITIONAL_ENUM_TYPE_UNEXPECTED_MEMBER_STRING_VALUE_DEFAULT = "unexpected";
    // package visibility for unit tests
    String BUILD_TIME_GLOBAL_PREFIX_FORMAT = "quarkus." + CODEGEN_TIME_CONFIG_PREFIX + ".%s";
    String BUILD_TIME_SPEC_PREFIX_FORMAT = "quarkus." + CODEGEN_TIME_CONFIG_PREFIX + ".spec.%s";

    List<String> SUPPORTED_CONFIGURATIONS = Arrays.stream(ConfigName.values()).map(cn -> cn.name)
            .collect(Collectors.toList());

    enum ConfigName {
        //global configs
        VERBOSE("verbose"),
        INPUT_BASE_DIR("input-base-dir"),
        TEMPLATE_BASE_DIR("template-base-dir"),
        INCLUDE("include"),
        EXCLUDE("exclude"),
        VALIDATE_SPEC("validateSpec"),
        DEFAULT_SECURITY_SCHEME("default-security-scheme"),

        //spec configs only
        BASE_PACKAGE("base-package"),
        API_NAME_SUFFIX("api-name-suffix"),
        MODEL_NAME_SUFFIX("model-name-suffix"),
        MODEL_NAME_PREFIX("model-name-prefix"),

        //global & spec configs
        SKIP_FORM_MODEL("skip-form-model"),
        MUTINY("mutiny"),
        MUTINY_RETURN_RESPONSE("mutiny.return-response"),
        MUTINY_OPERATION_IDS("mutiny.operation-ids"),
        ADDITIONAL_MODEL_TYPE_ANNOTATIONS("additional-model-type-annotations"),
        ADDITIONAL_ENUM_TYPE_UNEXPECTED_MEMBER("additional-enum-type-unexpected-member"),
        ADDITIONAL_ENUM_TYPE_UNEXPECTED_MEMBER_NAME("additional-enum-type-unexpected-member-name"),
        ADDITIONAL_ENUM_TYPE_UNEXPECTED_MEMBER_STRING_VALUE("additional-enum-type-unexpected-member-string-value"),
        ADDITIONAL_API_TYPE_ANNOTATIONS("additional-api-type-annotations"),
        TYPE_MAPPINGS("type-mappings"),
        IMPORT_MAPPINGS("import-mappings"),
        SCHEMA_MAPPINGS("schema-mappings"),
        NORMALIZER("open-api-normalizer"),
        RETURN_RESPONSE("return-response"),
        ENABLE_SECURITY_GENERATION("enable-security-generation"),
        CONFIG_KEY("config-key"),
        GENERATE_PART_FILENAME("generate-part-filename"),
        PART_FILENAME_VALUE("part-filename-value"),
        USE_FIELD_NAME_IN_PART_FILENAME("use-field-name-in-part-filename"),
        ADDITIONAL_PROPERTIES_AS_ATTRIBUTE("additional-properties-as-attribute"),
        ADDITIONAL_REQUEST_ARGS("additional-request-args"),
        REMOVE_OPERATION_ID_PREFIX("remove-operation-id-prefix"),
        REMOVE_OPERATION_ID_PREFIX_DELIMITER("remove-operation-id-prefix-delimiter"),
        REMOVE_OPERATION_ID_PREFIX_COUNT("remove-operation-id-prefix-count"),
        GENERATE_APIS("generate-apis"),
        GENERATE_MODELS("generate-models"),
        BEAN_VALIDATION("use-bean-validation"),
        SERIALIZABLE_MODEL("serializable-model"),
        EQUALS_HASHCODE("equals-hashcode"),
        USE_DYNAMIC_URL("use-dynamic-url");

        private final String name;

        ConfigName(String name) {
            this.name = name;
        }

    }

    /**
     * OpenAPI Spec details for codegen configuration.
     */
    @WithName("spec")
    Map<String, SpecItemConfig> specItem();

    static String resolveApiPackage(final String basePackage) {
        return String.format("%s%s", basePackage, API_PKG_SUFFIX);
    }

    static String resolveModelPackage(final String basePackage) {
        return String.format("%s%s", basePackage, MODEL_PKG_SUFFIX);
    }

    /**
     * Return global config name, openapi-generator.codegen.config-name
     */
    static String getGlobalConfigName(ConfigName configName) {
        return String.format(BUILD_TIME_GLOBAL_PREFIX_FORMAT, configName.name);
    }

    /**
     * Return spec config name openapi-generator.codegen.spec.%s.config-name
     */
    static String getSpecConfigName(ConfigName configName, final Path openApiFilePath) {
        return String.format("%s.%s", getBuildTimeSpecPropertyPrefix(openApiFilePath), configName.name);
    }

    /**
     * Return spec config name by config-key (<b>openapi-generator.codegen.spec.%s.config-key</b>) property.
     * For example, given a configuration <code>quarkus.openapi.generator.codegen.spec.spec_yaml.config-key=petstore</code>, the
     * returned value is
     * <code>openapi.generator.codegen.spec.petstore.mutiny</code>.
     */
    static String getSpecConfigNameByConfigKey(final String configKey, final ConfigName configName) {
        String buildTimeSpecPropertyPrefix = String.format(BUILD_TIME_SPEC_PREFIX_FORMAT, configKey);
        return String.format("%s.%s", buildTimeSpecPropertyPrefix, configName.name);
    }

    /**
     * Gets the config prefix for a given OpenAPI file in the path.
     * For example, given a path like /home/luke/projects/petstore.json, the returned value is
     * `quarkus.openapi-generator."petstore_json"`.
     * Every the periods (.) in the file name will be replaced by underscore (_).
     */
    static String getBuildTimeSpecPropertyPrefix(final Path openApiFilePath) {
        return String.format(BUILD_TIME_SPEC_PREFIX_FORMAT, getSanitizedFileName(openApiFilePath));
    }

    static String getSanitizedFileName(final Path openApiFilePath) {
        return StringUtil
                .replaceNonAlphanumericByUnderscores(OpenApiGeneratorOutputPaths.getRelativePath(openApiFilePath).toString());
    }
}
