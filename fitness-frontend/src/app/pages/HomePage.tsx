import { Hero } from '../components/Hero';
import { Schedule } from '../components/Schedule';
import { Trainers } from '../components/Trainers';
import { Pricing } from '../components/Pricing';

export function HomePage() {
  return (
    <>
      <Hero />
      <Schedule />
      <Trainers />
      <Pricing />
    </>
  );
}
