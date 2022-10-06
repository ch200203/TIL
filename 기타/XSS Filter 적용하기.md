## 프로젝트에 XSS 적용하기

### 1. lucy-xss-servlet-filter의 한계

 * lucy filter는 `form-data` 전송방식에는 유효하나, @RequestBody로 전달되는 JSON 요청은 처리 하지 않음.
 
 * 현재 사용되는 XSS처리가 통하지 않는 소스
 ```java
	@RequestMapping(value="/SendNote", method=RequestMethod.POST)
    public Map<String,Object> SendNote(@RequestBody Map<String,Object> inputs) throws Exception {
        Map<String,Object> output = new HashMap<String,Object>();
		output.put("prmt SendNote Controller", inputs);
		int count = service.SendNote(inputs);
		output.put("count", count);
		output.put("prmt", inputs);
		return output;
    }
 ```
 * 넘겨주는 데이터가 application/json, Map<String, Object>, @RequestBody
 * 기존의 XSS 필터가 적용되지 않아  XSS 방지를 새로 작성하여 처리해준다.
	
### 2. JSON API에 XSS 방지 처리


#### 2-1. HtmlCharacterEscapes 클래스 생성
```java
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import org.apache.commons.text.StringEscapeUtils;

public class HtmlCharacterEscapes extends CharacterEscapes {

    private final int[] asciiEscapes;

    public HtmlCharacterEscapes() {
        // 1. XSS 방지 처리할 특수 문자 지정
        asciiEscapes = CharacterEscapes.standardAsciiEscapesForJSON();
        asciiEscapes['<'] = CharacterEscapes.ESCAPE_CUSTOM;
        asciiEscapes['>'] = CharacterEscapes.ESCAPE_CUSTOM;
        asciiEscapes['\"'] = CharacterEscapes.ESCAPE_CUSTOM;
        asciiEscapes['('] = CharacterEscapes.ESCAPE_CUSTOM;
        asciiEscapes[')'] = CharacterEscapes.ESCAPE_CUSTOM;
        asciiEscapes['#'] = CharacterEscapes.ESCAPE_CUSTOM;
        asciiEscapes['\''] = CharacterEscapes.ESCAPE_CUSTOM;
    }

    @Override
    public int[] getEscapeCodesForAscii() {
        return asciiEscapes;
    }

    @Override
    public SerializableString getEscapeSequence(int ch) {
        return new SerializedString(StringEscapeUtils.escapeHtml4(Character.toString((char) ch)));
    }
}
```

 * `StringEscapeUtils` 사용을 위하여 build.gradle 의존성 주입  
	`implementation 'org.apache.commons:commons-text:1.8'`
	
#### 2-2. WebConfig에 MappingJackon2HttpMessage @Bean 등록

* HttpMessageConverter가 Bean으로 등록될 경우 Spring Context의 Converter 리스트에 이를 자동으로 추가해 줌

```java
	@Configuration
	public class WebConfiguration implements WebMvcConfigurer {
	
		private final ObjectMapper objectMapper;

		@Bean
		public MappingJackson2HttpMessageConverter jsonEscapeConverter() {
			ObjectMapper copy = objectMapper.copy();
			copy.getFactory().setCharacterEscapes(new HtmlCharacterEscapes());
			return new MappingJackson2HttpMessageConverter(copy);
		}
	}
```


### 3. XSS Filter를 적용 

 * POST으로 전달된 "application/json" 타입의 데이터를 Filter에서 사용하기 위해서는  HttpServletRequest의 InputStream을 읽어 들여야 한다.
 * 그러나, HttpServletRequest의 InputStream은 한 번 읽으면 다시 읽을 수 없음 => Tomcat이 이를 막음
 * 따라서, wrapper 객체를 만들어 InputStream을 읽어서 다시 돌려주는 방식으로 개발해야함.

#### 3-1 Filter 클래스 생성
```java
@Component
public class XSSFilter implements Filter {

    public FilterConfig filterConfig;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    	chain.doFilter(new XSSFilterWrapper((HttpServletRequest) request), response);
    }

    @Override
	public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
	}

	@Override
	public void destroy() {
        this.filterConfig = null;
	}
}

```

* web.xml에 정의할 필터를 생성
* Filter 클래스를 implements => doFilter 메서드에서 request 객체를 통해 url, method에 대해 커스터마이징이 가능.
* doFilter에서 XSSFilterWrapper를 new로 생성

#### 3-2. XSSFilterWrapper에서 클래스 생성 후 XSS 적용

```java
public class XSSFilterWrapper extends HttpServletRequestWrapper {
    
    private byte[] rawData;
    
    public XSSFilterWrapper(HttpServletRequest request) {
        super(request);
        try {
            InputStream inputStream = request.getInputStream();
            this.rawData = replaceXSS(IOUtils.toByteArray(inputStream));
        } catch (Exception e) {
			e.printStackTrace();
        }
    }

    // XSS replace
    private byte[] replaceXSS(byte[] data) {
        String strData = new String(data);
        strData = strData.replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;").replaceAll("\\(", "&#40;").replaceAll("\\)", "&#41;");
		
		return strData.getBytes();
    }

    private String replaceXSS(String value) {
        if(value != null) {
			value = value.replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;").replaceAll("\\(", "&#40;").replaceAll("\\)", "&#41;");
		}
		return value;
    }
    
    //새로운 인풋스트림을 리턴하지 않으면 에러가 남
	@Override
	public ServletInputStream getInputStream() throws IOException {
		if(this.rawData == null) {
			return super.getInputStream();
		}
        
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.rawData);
		
		return new ServletInputStream() {
			
			@Override
			public int read() throws IOException {
				// TODO Auto-generated method stub
				return byteArrayInputStream.read();
			}
			
			@Override
			public void setReadListener(ReadListener readListener) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isReady() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isFinished() {
				// TODO Auto-generated method stub
				return false;
			}
		};
	}

	@Override
	public String getQueryString() {
		return replaceXSS(super.getQueryString());
	}


	@Override
	public String getParameter(String name) {
		return replaceXSS(super.getParameter(name));
	}


	@Override
	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> params = super.getParameterMap();
		if(params != null) {
			params.forEach((key, value) -> {
				for(int i=0; i<value.length; i++) {
					value[i] = replaceXSS(value[i]);
				}
			});
		}
		return params;
	}


	@Override
	public String[] getParameterValues(String name) {
		String[] params = super.getParameterValues(name);
		if(params != null) {
			for(int i=0; i<params.length; i++) {
				params[i] = replaceXSS(params[i]);
			}
		}
		return params;
	}


	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(this.getInputStream(), "UTF_8"));
	}


    private static Pattern[] patterns = new Pattern[] {
        Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
    };

    private String stripXSS(String value) {
        //System.out.println("stripXSS: value = "+value);
        if (value != null) {

            // NOTE: It's highly recommended to use the ESAPI library and
            // uncomment the following line to
            // avoid encoded attacks.
            // value = ESAPI.encoder().canonicalize(value);
            // Avoid null characters

            value = value.replaceAll("\0", "");

            // Remove all sections that match a pattern
            for(Pattern scriptPattern : patterns){
                if(scriptPattern.matcher(value).matches()){
                    value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");        
                }
            }
            value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("'","&apos;");
        }
        //System.out.println("result = "+value);
        return value;
    }
}
```

 * replaceXSS 메소드
	@RequestBody용 btye[]를 받는 메소드와 그 외 용으로 String을 받는 메소드를 오버로드하여 사용.  
	
	안에 replace 로직은 간단하게 <, >, (, ) 만 구현 =>  하단에 상세한 로직 추가하여 필요한 경우 가져다 쓰는 것이 가능
	
 * form-data filtering
	form-data, getParameter를 이용하여 데이터를 가져올때는 getQueryString과 getParameter 메소드를 사용하기 때문에
	해당하는 메소드를 오버라이딩 하여 사용.  
	
	getQueryString과 getParameter 메소드는 리턴할때 원래 넘어가는 super객체의 값에 구현해둔 replaceXSS를 씌워서 넘겨준다.  
	
	getParameterValues와 getParameterMap 메소드도 배열과 Map을 리턴이 가능하기 떄문에 안에서 XSS 처리를 해줌.  
	
 * json filtering (핵심)
	json의 경우 Content-type이 application/json 그리고 @RequestBody에 데이터가 row로 날라오기 때문에 그 해당 데이터를 필터에서 먼저 읽어서 XSS 처리를 해주고, 그 후에 응답을 보내기위해 InputStream을 호출하기 때문에 getInputStream을 오버라이딩해서 처리된 rawData를 넣어준다.
	
	
#### 3-4. web.xml에 필터를 등록
```xml
 <!-- XSS 	-->
  	<filter>
        <filter-name>xss</filter-name>
        <filter-class>com.haniln.spring.filter.XSSFilter</filter-class>
    </filter>
    <filter-mapping>
		<filter-name>xss</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>
```



### 4. 참고자료

1. [참고자료](https://meetup.toast.com/posts/44)
2. [참고자료](https://riverblue.tistory.com/m/46)
3. [참고자료](https://homoefficio.github.io/2016/11/21/Spring%EC%97%90%EC%84%9C-JSON%EC%97%90-XSS-%EB%B0%A9%EC%A7%80-%EC%B2%98%EB%A6%AC-%ED%95%98%EA%B8%B0/)
4. [참고자료](https://riverblue.tistory.com/m/46)
	