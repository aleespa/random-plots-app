#include "sketch_base.h"
#include <random>
#include <cmath>
#include <complex>
#include <vector>
#include <Eigen/Dense>

class RandomEigenSketch : public SketchBase {
public:
    void render(RenderContext& ctx, uint64_t seed) override {
        std::mt19937_64 rng(seed);
        std::uniform_int_distribution<int> dist_n(3, 11);
        std::uniform_int_distribution<int> dist_m(-1, 1);

        int n = dist_n(rng);
        int sample_size = 10000;

        Eigen::MatrixXcf m = Eigen::MatrixXcf::Zero(n, n);
        for(int i=0; i<n; ++i) {
            for(int j=0; j<n; ++j) {
                m(i, j) = std::complex<float>(dist_m(rng), dist_m(rng));
            }
        }

        std::vector<std::pair<int, int>> idx(3);
        std::uniform_int_distribution<int> dist_idx(0, n - 1);
        for(int i=0; i<3; ++i) idx[i] = {dist_idx(rng), dist_idx(rng)};

        std::vector<Point2f> all_eigvals;
        all_eigvals.reserve(sample_size * n);

        float sum_x = 0, sum_y = 0;

        for (int i = 0; i < sample_size; ++i) {
            Eigen::MatrixXcf mt = m;
            for(int k=0; k<3; ++k) {
                float theta = std::uniform_real_distribution<float>(0, 2.0f * M_PI)(rng);
                mt(idx[k].first, idx[k].second) = std::polar(1.0f, theta);
            }

            Eigen::ComplexEigenSolver<Eigen::MatrixXcf> solver(mt, false);
            auto evals = solver.eigenvalues();
            for(int j=0; j<evals.size(); ++j) {
                Point2f p = {evals[j].real(), evals[j].imag()};
                all_eigvals.push_back(p);
                sum_x += p.x;
                sum_y += p.y;
            }
        }

        float mean_x = sum_x / all_eigvals.size();
        float mean_y = sum_y / all_eigvals.size();
        ctx.setWorldBounds(mean_x - 8, mean_x + 8, mean_y - 8, mean_y + 8);

        // Split into chunks to assign different colors if needed, or just one call
        ctx.drawScatter(all_eigvals, ctx.getColor(std::uniform_real_distribution<float>(0, 1)(rng)), 2.0f, 0.9f);
    }
};
