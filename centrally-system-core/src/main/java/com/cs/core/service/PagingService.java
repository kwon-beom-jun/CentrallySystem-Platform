package com.cs.core.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.cs.core.vo.page.PageResponseVo;


/*
	※※ 서비스에서 공통 페이징 로직 사용하기 ※※
	이제 각 엔티티 서비스에서 PagingService를 호출하여 페이징을 처리할 수 있습니다.
	
	ProductService에서 공통 페이징 적용
	java
	코드 복사
	@Service
	public class ProductService {
	    @Autowired
	    private ProductRepository productRepository;
	
	    @Autowired
	    private PagingService pagingService;
	
	    public PageResponseVo<Product> getProducts(PageRequestVo pageRequestVo) {
	        Pageable pageable = pageRequestVo.toPageable();
	        return pagingService.getPagedData(pageable, productRepository);
	    }
	}
	
	
	※ 컨트롤러에서 요청 처리 ※
	컨트롤러에서 페이징 처리를 호출할 때는 PageRequestVo를 사용하여 클라이언트로부터 페이징 정보를 받아 처리할 수 있습니다.
	
	java
	코드 복사
	@RestController
	@RequestMapping("/products")
	public class ProductController {
	    @Autowired
	    private ProductService productService;
	
	    @GetMapping
	    public ResponseEntity<PageResponseVo<Product>> getProducts(PageRequestVo pageRequestVo) {
	        PageResponseVo<Product> products = productService.getProducts(pageRequestVo);
	        return new ResponseEntity<>(products, HttpStatus.OK);
	    }
	}
*/
@Service
public class PagingService {

    public <T> PageResponseVo<T> getPagedData(Pageable pageable, JpaRepository<T, Long> repository) {
        Page<T> pageData = repository.findAll(pageable);
        return new PageResponseVo<T>(pageData);
    }
}
