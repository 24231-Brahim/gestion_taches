import { afterEach, beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpHeaders } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { faLayerGroup, faSync, faPlus, faTimes, faSearch, faFilter } from '@fortawesome/free-solid-svg-icons';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';

import { ITEMS_PER_PAGE } from 'app/config/pagination.constants';
import { IEpic } from '../epic.model';
import { EpicService } from '../service/epic.service';
import { EpicRoadmap } from './epic-roadmap';
import { sampleWithRequiredData, sampleWithFullData } from '../epic.test-samples';

describe('EpicRoadmap', () => {
  let fixture: ComponentFixture<EpicRoadmap>;
  let comp: EpicRoadmap;
  let epicServiceMock: any;
  let routerMock: any;

  const mockEpics: IEpic[] = [
    { ...sampleWithRequiredData, status: 'IN_PROGRESS' },
    { ...sampleWithFullData, status: 'DONE' },
  ];

  beforeEach(() => {
    epicServiceMock = {
      epicsResource: {
        isLoading: vitest.fn().mockReturnValue(false),
        headers: vitest.fn().mockReturnValue(new HttpHeaders({ 'X-Total-Count': '10' })),
      },
      epics: vitest.fn().mockReturnValue([]),
      epicsParams: { set: vitest.fn() },
      getEpicIdentifier: (item: IEpic) => item.id,
    };
    epicServiceMock.epicsResource.isLoading = vitest.fn().mockReturnValue(false);
    epicServiceMock.epics = vitest.fn().mockReturnValue([]);

    TestBed.configureTestingModule({
      imports: [FontAwesomeModule, TranslateModule.forRoot(), EpicRoadmap],
      providers: [provideRouter([]), { provide: EpicService, useValue: epicServiceMock }],
    });

    const library = TestBed.inject(FaIconLibrary);
    library.addIcons(faLayerGroup, faSync, faPlus);

    fixture = TestBed.createComponent(EpicRoadmap);
    comp = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    TestBed.resetTestingModule();
  });

  it('should create', () => {
    expect(comp).toBeTruthy();
  });

  it('filteredEpics should return all epics when no filter', () => {
    comp.epics.set(mockEpics);
    fixture.detectChanges();
    expect(comp.filteredEpics().length).toBe(2);
  });

  it('filteredEpics should filter by status', () => {
    comp.epics.set(mockEpics);
    comp.filterStatus.set('DONE');
    fixture.detectChanges();
    expect(comp.filteredEpics().length).toBe(1);
    expect(comp.filteredEpics()[0].status).toBe('DONE');
  });

  it('filteredEpics should return empty when filter matches nothing', () => {
    comp.epics.set(mockEpics);
    comp.filterStatus.set('CANCELLED');
    fixture.detectChanges();
    expect(comp.filteredEpics().length).toBe(0);
  });

  it('epicsWithProgress should map to EpicWithProgress', () => {
    comp.epics.set(mockEpics);
    fixture.detectChanges();
    const items = comp.epicsWithProgress();
    expect(items.length).toBe(2);
    expect(items[0].progress).toBe(0);
    expect(items[0].totalIssues).toBe(0);
    expect(items[0].doneIssues).toBe(0);
  });

  it('getBarStyle should return width and background style', () => {
    comp.epics.set(mockEpics);
    fixture.detectChanges();
    const item = comp.epicsWithProgress()[0];
    const style = comp.getBarStyle(item);
    expect(style.width).toBe('0%');
    expect(style.background).toBeTruthy();
  });

  it('getBarRadius should return borderRadius based on progress', () => {
    comp.epics.set(mockEpics);
    fixture.detectChanges();
    const item = comp.epicsWithProgress()[0];
    const below100 = comp.getBarRadius({ ...item, progress: 50 });
    expect(below100.borderRadius).toBe('6px 0 0 6px');
    const at100 = comp.getBarRadius({ ...item, progress: 100 });
    expect(at100.borderRadius).toBe('6px');
  });

  it('getEpicDates should format start-end dates', () => {
    const result = comp.getEpicDates(mockEpics[1]);
    expect(result).toContain('Jun 1');
    expect(result).toContain('Jun 30');
  });

  it('getEpicDates should return createdAt when no start/end dates', () => {
    const result = comp.getEpicDates(mockEpics[0]);
    expect(result).toContain('Jun');
  });

  it('getEpicDates should return empty for entity with no dates', () => {
    const result = comp.getEpicDates({ ...sampleWithRequiredData, createdAt: undefined, startDate: undefined, endDate: undefined });
    expect(result).toBe('');
  });

  it('queryBackend should set epicsParams', () => {
    (comp as any).queryBackend();
    expect(epicServiceMock.epicsParams.set).toHaveBeenCalled();
    const callArg = epicServiceMock.epicsParams.set.mock.calls[0][0];
    expect(callArg.page).toBe(0);
    expect(callArg.size).toBe(ITEMS_PER_PAGE);
    expect(callArg.eagerload).toBe(true);
  });

  it('handleNavigation should navigate with query params', () => {
    const navigateSpy = vitest.spyOn(comp.router, 'navigate');
    (comp as any).handleNavigation(2, { predicate: 'id', order: 'asc' });
    expect(navigateSpy).toHaveBeenCalledWith(
      ['./'],
      expect.objectContaining({
        queryParams: { page: 2, size: ITEMS_PER_PAGE, sort: 'id,asc' },
      }),
    );
  });

  it('trackId should forward to epicService', () => {
    const entity = { id: 42 };
    vitest.spyOn(epicServiceMock, 'getEpicIdentifier');
    comp.trackId(entity);
    expect(epicServiceMock.getEpicIdentifier).toHaveBeenCalledWith(entity);
  });
});
