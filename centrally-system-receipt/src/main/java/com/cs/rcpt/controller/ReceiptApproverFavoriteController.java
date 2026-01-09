package com.cs.rcpt.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.cs.rcpt.controller.request.ApproverFavoriteDto;
import com.cs.rcpt.service.ReceiptApproverFavoriteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/approver-favorites", name = "ReceiptApproverFavorite")
@RequiredArgsConstructor
public class ReceiptApproverFavoriteController {

	private final ReceiptApproverFavoriteService service;

	@GetMapping("/{ownerUserId}")
	public List<ApproverFavoriteDto> getApproverFavorites(@PathVariable(name = "ownerUserId") Long ownerUserId) {
		return service.getApproverFavorites(ownerUserId);
	}

	@PostMapping
	public ApproverFavoriteDto createApproverFavorite(@RequestBody ApproverFavoriteDto dto) {
		return service.createApproverFavorite(dto);
	}

	@DeleteMapping
	public void deleteApproverFavorite(
			@RequestParam(name = "ownerUserId") Long ownerUserId,
			@RequestParam(name = "favoriteUserId") Long favoriteUserId) {
		service.deleteApproverFavorite(ownerUserId, favoriteUserId);
	}

	@PatchMapping("/order/{ownerUserId}")
	public void udpateApproverFavorite(
			@PathVariable(name = "ownerUserId") Long ownerUserId, 
			@RequestBody List<ApproverFavoriteDto> orderList) {
		service.udpateApproverFavorite(ownerUserId, orderList);
	}

	/** 권한 상실 사용자에 대한 즐겨찾기 전체 제거 - 내부용 */
	@DeleteMapping("/internal/remove-by-fav/{favoriteUserId}")
	public void deleteAllByFavoriteUser(@PathVariable(name = "favoriteUserId") Long favoriteUserId) {
		service.deleteAllByFavoriteUser(favoriteUserId);
	}
}


