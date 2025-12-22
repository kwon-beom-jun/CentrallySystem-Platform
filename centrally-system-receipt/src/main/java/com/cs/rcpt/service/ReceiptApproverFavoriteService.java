package com.cs.rcpt.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cs.rcpt.controller.request.ApproverFavoriteDto;
import com.cs.rcpt.entity.ReceiptApproverFavorite;
import com.cs.rcpt.repository.ReceiptApproverFavoriteRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReceiptApproverFavoriteService {

	private final ReceiptApproverFavoriteRepository repository;

	@Transactional
	public List<ApproverFavoriteDto> getApproverFavorites(Long ownerUserId) {
		return repository.findByOwnerUserIdOrderByStepNoAsc(ownerUserId)
			.stream()
			.map(this::toDto)
			.toList();
	}

	@Transactional
	public ApproverFavoriteDto createApproverFavorite(ApproverFavoriteDto dto) {
		if (repository.existsByOwnerUserIdAndFavoriteUserId(dto.getOwnerUserId(), dto.getFavoriteUserId())) {
			return getApproverFavorites(dto.getOwnerUserId())
				.stream()
				.filter(x -> x.getFavoriteUserId().equals(dto.getFavoriteUserId()))
				.findFirst()
				.orElseThrow();
		}
		int nextOrder = repository.findMaxStepNoByOwner(dto.getOwnerUserId()) + 1;
		ReceiptApproverFavorite saved = repository.save(ReceiptApproverFavorite.builder()
				.ownerUserId(dto.getOwnerUserId())
				.favoriteUserId(dto.getFavoriteUserId())
				.favoriteUserName(dto.getFavoriteUserName())
				.email(dto.getEmail())
				.department(dto.getDepartment())
				.team(dto.getTeam())
				.stepNo(nextOrder)
				.build());
		return toDto(saved);
	}

	@Transactional
	public void deleteApproverFavorite(Long ownerUserId, Long favoriteUserId) {
		repository.deleteByOwnerUserIdAndFavoriteUserId(ownerUserId, favoriteUserId);
		// 순서 재정렬 (소유자별)
		List<ReceiptApproverFavorite> list = repository.findByOwnerUserIdOrderByStepNoAsc(ownerUserId);
		int idx = 1;
		for (ReceiptApproverFavorite f : list) {
			f.setStepNo(idx++);
		}
		repository.saveAll(list);
	}

	@Transactional
	public void udpateApproverFavorite(Long ownerUserId, List<ApproverFavoriteDto> orderList) {
		// orderList: [{favoriteUserId, stepNo}, ...]
		Map<Long,Integer> map = orderList.stream().collect(Collectors.toMap(ApproverFavoriteDto::getFavoriteUserId, ApproverFavoriteDto::getStepNo));
		List<ReceiptApproverFavorite> list = repository.findByOwnerUserIdOrderByStepNoAsc(ownerUserId);
		for (ReceiptApproverFavorite f : list) {
			Integer step = map.get(f.getFavoriteUserId());
			if (step != null) f.setStepNo(step);
		}
		list.sort(Comparator.comparing(ReceiptApproverFavorite::getStepNo));
		int idx = 1;
		for (ReceiptApproverFavorite f : list) {
			f.setStepNo(idx++);
		}
		repository.saveAll(list);
	}

	/** 권한 상실 사용자에 대한 즐겨찾기 전체 제거 */
	@Transactional
	public void deleteAllByFavoriteUser(Long favoriteUserId) {
		repository.deleteByFavoriteUserId(favoriteUserId);
	}

	private ApproverFavoriteDto toDto(ReceiptApproverFavorite e) {
		return ApproverFavoriteDto.builder()
				.id(e.getId())
				.ownerUserId(e.getOwnerUserId())
				.favoriteUserId(e.getFavoriteUserId())
				.favoriteUserName(e.getFavoriteUserName())
				.email(e.getEmail())
				.department(e.getDepartment())
				.team(e.getTeam())
				.stepNo(e.getStepNo())
				.build();
	}
}


