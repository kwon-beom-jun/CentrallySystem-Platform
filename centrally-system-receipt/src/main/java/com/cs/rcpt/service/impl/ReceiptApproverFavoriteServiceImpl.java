package com.cs.rcpt.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cs.rcpt.controller.request.ApproverFavoriteDto;
import com.cs.rcpt.entity.ReceiptApproverFavorite;
import com.cs.rcpt.repository.ReceiptApproverFavoriteRepository;
import com.cs.rcpt.service.ReceiptApproverFavoriteService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * 영수증 결재자 즐겨찾기 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class ReceiptApproverFavoriteServiceImpl implements ReceiptApproverFavoriteService {

	private final ReceiptApproverFavoriteRepository repository;

	@Override
	@Transactional
	public List<ApproverFavoriteDto> getApproverFavorites(Long ownerUserId) {
		return repository.findByOwnerUserIdOrderByStepNoAsc(ownerUserId)
			.stream()
			.map(this::toDto)
			.toList();
	}

	@Override
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

	@Override
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

	@Override
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
	@Override
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

