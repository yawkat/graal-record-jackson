package example;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.CreatorProperty;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotationCollector;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.fasterxml.jackson.databind.introspect.VirtualAnnotatedMember;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.util.NativeImageUtil;
import com.fasterxml.jackson.databind.util.SimpleBeanPropertyDefinition;
import org.graalvm.nativeimage.ImageInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;

public class RecordTest {
    @Test
    public void nativeImageUtilDoesNotThrow() throws ClassNotFoundException {
        NativeImageUtil.needsReflectionConfiguration(Rec.class);
    }

    @Test
    public void recordSerNoModifier() throws JsonProcessingException {
        Assumptions.assumeTrue(ImageInfo.inImageRuntimeCode());
        ObjectMapper mapper = JsonMapper.builder()
                .build();
        Assertions.assertThrows(
                InvalidDefinitionException.class,
                () -> mapper.writeValueAsString(new Rec("bar")),
                "No serializer found for class example.Rec and no properties discovered to create BeanSerializer (to avoid exception, disable SerializationFeature.FAIL_ON_EMPTY_BEANS). This appears to be a native image, in which case you may need to configure reflection for the class that is to be serialized"
        );
    }

    @Test
    public void recordDeserNoModifier() {
        Assumptions.assumeTrue(ImageInfo.inImageRuntimeCode());
        ObjectMapper mapper = JsonMapper.builder()
                .build();
        Assertions.assertThrows(
                InvalidDefinitionException.class,
                () -> mapper.readValue("{\"foo\":\"bar\"}", Rec.class),
                "Cannot construct instance of `example.Rec`: cannot deserialize from Object value (no delegate- or property-based Creator): this appears to be a native image, in which case you may need to configure reflection for the class that is to be deserialized"
        );
    }

    @Test
    public void recordDeserModifier() throws JsonProcessingException {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new SimpleModule() {{
                    setSerializerModifier(new RecordSerializerModifier());
                    setDeserializerModifier(new RecordDeserializerModifier());
                }})
                .build();
        Assertions.assertEquals("bar", mapper.readValue("{\"foo\":\"bar\"}", Rec.class).foo());
    }

    @Test
    public void recordSerModifier() throws JsonProcessingException {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new SimpleModule() {{
                    setSerializerModifier(new RecordSerializerModifier());
                    setDeserializerModifier(new RecordDeserializerModifier());
                }})
                .build();
        Assertions.assertEquals("{\"foo\":\"bar\"}", mapper.writeValueAsString(new Rec("bar")));
    }

    @Test
    public void normalBeanSer() throws JsonProcessingException {
        Assumptions.assumeTrue(ImageInfo.inImageRuntimeCode());
        ObjectMapper mapper = JsonMapper.builder()
                .build();
        Assertions.assertThrows(
                InvalidDefinitionException.class,
                () -> mapper.writeValueAsString(new Bean("bar")),
                "No serializer found for class example.Bean and no properties discovered to create BeanSerializer (to avoid exception, disable SerializationFeature.FAIL_ON_EMPTY_BEANS). This appears to be a native image, in which case you may need to configure reflection for the class that is to be serialized"
        );
    }

    @Test
    public void normalBeanDeser() throws JsonProcessingException {
        Assumptions.assumeTrue(ImageInfo.inImageRuntimeCode());
        ObjectMapper mapper = JsonMapper.builder()
                .build();
        Assertions.assertThrows(
                InvalidDefinitionException.class,
                () -> mapper.readValue("{\"foo\":\"bar\"}", Bean.class),
                "Cannot construct instance of `example.Bean`: cannot deserialize from Object value (no delegate- or property-based Creator): this appears to be a native image, in which case you may need to configure reflection for the class that is to be deserialized"
        );
    }

    @Test
    public void configuredRecordSer() throws JsonProcessingException {
        ObjectMapper mapper = JsonMapper.builder()
                .build();
        Assertions.assertEquals("{\"foo\":\"bar\"}", mapper.writeValueAsString(new RecWithConfig("bar")));
    }

    @Test
    public void configuredRecordDeser() throws JsonProcessingException {
        ObjectMapper mapper = JsonMapper.builder()
                .build();
        Assertions.assertEquals("bar", mapper.readValue("{\"foo\":\"bar\"}", RecWithConfig.class).foo());
    }

    @Test
    public void configuredBeanSer() throws JsonProcessingException {
        ObjectMapper mapper = JsonMapper.builder()
                .build();
        Assertions.assertEquals("{\"foo\":\"bar\"}", mapper.writeValueAsString(new BeanWithConfig("bar")));
    }

    @Test
    public void configuredBeanDeser() throws JsonProcessingException {
        ObjectMapper mapper = JsonMapper.builder()
                .build();
        Assertions.assertEquals("bar", mapper.readValue("{\"foo\":\"bar\"}", BeanWithConfig.class).getFoo());
    }

    private static class RecordDeserializerModifier extends BeanDeserializerModifier {
        @Override
        public BeanDeserializerBuilder updateBuilder(DeserializationConfig config, BeanDescription beanDesc, BeanDeserializerBuilder builder) {
            if (beanDesc.getBeanClass().equals(Rec.class)) {
                builder.setValueInstantiator(new StdValueInstantiator(config, config.constructType(Rec.class)) {
                    @Override
                    public SettableBeanProperty[] getFromObjectArguments(DeserializationConfig config) {
                        return new SettableBeanProperty[]{
                                CreatorProperty.construct(
                                        PropertyName.construct("foo"),
                                        config.constructType(String.class),
                                        null, null, null, null, 0, null,
                                        PropertyMetadata.STD_REQUIRED
                                )
                        };
                    }

                    @Override
                    public boolean canCreateFromObjectWith() {
                        return true;
                    }

                    @Override
                    public Object createFromObjectWith(DeserializationContext ctxt, Object[] args) throws IOException {
                        return new Rec((String) args[0]);
                    }
                });
            }

            return builder;
        }
    }

    private static class RecordSerializerModifier extends BeanSerializerModifier {
        @Override
        public BeanSerializerBuilder updateBuilder(SerializationConfig config, BeanDescription beanDesc, BeanSerializerBuilder builder) {
            if (beanDesc.getBeanClass().equals(Rec.class)) {
                AnnotatedMember virtualMember = new VirtualAnnotatedMember(new TypeResolutionContext.Empty(config.getTypeFactory()), Rec.class, "foo", config.constructType(String.class));
                builder.setProperties(Collections.singletonList(new BeanPropertyWriter(
                        SimpleBeanPropertyDefinition.construct(config, virtualMember),
                        virtualMember,
                        AnnotationCollector.emptyAnnotations(),
                        config.constructType(String.class),
                        null, null, null, false, null, null
                ) {
                    @Override
                    public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
                        gen.writeFieldName("foo");
                        _serializer.serialize(((Rec) bean).foo(), gen, prov);
                    }
                }));
            }
            return builder;
        }
    }
}
