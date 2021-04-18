import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { WaitingLogoComponent } from './waiting-logo.component';

describe('WaitingLogoComponent', () => {
  let component: WaitingLogoComponent;
  let fixture: ComponentFixture<WaitingLogoComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WaitingLogoComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WaitingLogoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
